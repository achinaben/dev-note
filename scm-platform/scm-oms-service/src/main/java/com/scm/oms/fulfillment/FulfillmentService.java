package com.scm.oms.fulfillment;

import com.scm.oms.integration.TmsFulfillmentClient;
import com.scm.oms.integration.WmsGateway;
import org.springframework.beans.factory.annotation.Value;
import com.scm.oms.order.OrderRecord;
import com.scm.oms.order.OrderRepository;
import com.scm.oms.order.OrderStateMachine;
import com.scm.oms.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FulfillmentService {
    private final PackageRepository packageRepository;
    private final WmsGateway wmsClient;
    private final OrderRepository orderRepository;
    private final OrderStateMachine stateMachine;
    private final TmsFulfillmentClient tmsClient;

    @Value("${scm.fulfillment.auto-tms-on-ship:true}")
    private boolean autoTmsOnShip;

    public FulfillmentService(
            PackageRepository packageRepository,
            WmsGateway wmsClient,
            OrderRepository orderRepository,
            OrderStateMachine stateMachine,
            TmsFulfillmentClient tmsClient) {
        this.packageRepository = packageRepository;
        this.wmsClient = wmsClient;
        this.orderRepository = orderRepository;
        this.stateMachine = stateMachine;
        this.tmsClient = tmsClient;
    }

    public PackageRecord releaseAfterPaid(OrderRecord order) {
        return packageRepository.findByOrderNo(order.getOrderNo()).orElseGet(() -> {
            PackageRecord pkg = new PackageRecord();
            pkg.setOrderNo(order.getOrderNo());
            pkg.setPackageNo("P" + order.getOrderNo());
            String outboundNo = wmsClient.findOutboundByOrder(order.getOrderNo())
                    .orElseGet(() -> wmsClient.createOutbound(pkg.getPackageNo(), order.getOrderNo()));
            pkg.setOutboundNo(outboundNo);
            pkg.setStatus("WMS_CREATED");
            packageRepository.save(pkg);
            if (order.getStatus() == OrderStatus.PAID) {
                order.setStatus(stateMachine.apply(order.getStatus(), OrderStatus.FULFILLING));
                orderRepository.update(order);
            }
            return pkg;
        });
    }

    public List<PackageRecord> initTwoPackages(String orderNo) {
        OrderRecord order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        String p1 = "P" + orderNo + "-1";
        String p2 = "P" + orderNo + "-2";
        createPackageIfAbsent(order, p1);
        createPackageIfAbsent(order, p2);
        if (order.getStatus().rank() < OrderStatus.FULFILLING.rank()) {
            order.setStatus(stateMachine.apply(order.getStatus(), OrderStatus.FULFILLING));
            orderRepository.update(order);
        }
        return packageRepository.findAllByOrderNo(orderNo);
    }

    private void createPackageIfAbsent(OrderRecord order, String packageNo) {
        if (packageRepository.findByPackageNo(packageNo).isPresent()) {
            return;
        }
        PackageRecord pkg = new PackageRecord();
        pkg.setOrderNo(order.getOrderNo());
        pkg.setPackageNo(packageNo);
        String outboundNo = wmsClient.createOutbound(packageNo, order.getOrderNo());
        pkg.setOutboundNo(outboundNo);
        pkg.setStatus("WMS_CREATED");
        packageRepository.save(pkg);
    }

    public void shipPackage(String orderNo, String packageNo) {
        PackageRecord pkg = packageRepository.findByPackageNo(packageNo)
                .orElseThrow(() -> new IllegalArgumentException("OMS_10020"));
        if (!"SHIPPED".equals(pkg.getStatus())) {
            String waybill = "WB-" + packageNo;
            wmsClient.ship(pkg.getOutboundNo(), waybill);
            pkg.setStatus("SHIPPED");
            packageRepository.save(pkg);
        }
        refreshOrderShipStatus(orderNo);
        autoCreateTmsShipment(packageNo, orderNo, "WB-" + packageNo);
    }

    public void markShipped(String orderNo) {
        OrderRecord order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        List<PackageRecord> packages = packageRepository.findAllByOrderNo(orderNo);
        if (packages.isEmpty()) {
            releaseAfterPaid(order);
            packages = packageRepository.findAllByOrderNo(orderNo);
        }
        for (PackageRecord pkg : packages) {
            if (!"SHIPPED".equals(pkg.getStatus())) {
                String waybill = "WB-" + pkg.getPackageNo();
                wmsClient.ship(pkg.getOutboundNo(), waybill);
                pkg.setStatus("SHIPPED");
                packageRepository.save(pkg);
                autoCreateTmsShipment(pkg.getPackageNo(), orderNo, waybill);
            }
        }
        refreshOrderShipStatus(orderNo);
    }

    private void refreshOrderShipStatus(String orderNo) {
        OrderRecord order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        List<PackageRecord> packages = packageRepository.findAllByOrderNo(orderNo);
        if (packages.isEmpty()) {
            return;
        }
        long shipped = packages.stream().filter(p -> "SHIPPED".equals(p.getStatus())).count();
        OrderStatus target;
        if (shipped == 0) {
            return;
        } else if (shipped < packages.size()) {
            target = OrderStatus.PARTIAL_SHIPPED;
        } else {
            target = OrderStatus.SHIPPED;
        }
        if (order.getStatus().rank() < target.rank()) {
            order.setStatus(stateMachine.apply(order.getStatus(), target));
            orderRepository.update(order);
        }
    }

    public void markDelivered(String orderNo) {
        OrderRecord order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        if (order.getStatus().rank() < OrderStatus.DELIVERED.rank()) {
            order.setStatus(stateMachine.apply(order.getStatus(), OrderStatus.DELIVERED));
            orderRepository.update(order);
        }
    }

    /**
     * 消费 WMS_OUTBOUND_SHIPPED 事件：包裹已发运，刷新订单为 SHIPPED / PARTIAL_SHIPPED。
     */
    public void applyWmsShippedEvent(String orderNo, String packageNo, String outboundNo) {
        applyWmsShippedEvent(orderNo, packageNo, outboundNo, null);
    }

    public void applyWmsShippedEvent(String orderNo, String packageNo, String outboundNo, String waybillNo) {
        PackageRecord pkg = packageRepository.findByPackageNo(packageNo).orElseGet(() -> {
            PackageRecord created = new PackageRecord();
            created.setOrderNo(orderNo);
            created.setPackageNo(packageNo);
            return created;
        });
        pkg.setOutboundNo(outboundNo);
        pkg.setStatus("SHIPPED");
        packageRepository.save(pkg);
        refreshOrderShipStatus(orderNo);
        autoCreateTmsShipment(packageNo, orderNo, waybillNo);
    }

    private void autoCreateTmsShipment(String packageNo, String orderNo) {
        autoCreateTmsShipment(packageNo, orderNo, null);
    }

    private void autoCreateTmsShipment(String packageNo, String orderNo, String waybillNo) {
        if (!autoTmsOnShip) {
            return;
        }
        try {
            tmsClient.createShipmentWithWaybill(packageNo, orderNo, waybillNo);
        } catch (Exception ignored) {
            // 联调/TMS 未就绪时不阻断发运主流程
        }
    }
}
