package com.scm.oms.aftersale;

import com.scm.oms.fulfillment.PackageRepository;
import com.scm.oms.integration.ErpRefundClient;
import com.scm.oms.integration.TmsInterceptClient;
import com.scm.oms.order.OrderRepository;
import com.scm.oms.order.OrderStateMachine;
import com.scm.oms.order.OrderStatus;
import com.scm.oms.outbox.OutboxEvent;
import com.scm.oms.outbox.OutboxStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AfterSaleService {
    private final AfterSaleRepository afterSaleRepository;
    private final OrderRepository orderRepository;
    private final OrderStateMachine stateMachine;
    private final OutboxStore outboxStore;
    private final ErpRefundClient erpRefundClient;
    private final PackageRepository packageRepository;
    private final TmsInterceptClient tmsInterceptClient;

    public AfterSaleService(
            AfterSaleRepository afterSaleRepository,
            OrderRepository orderRepository,
            OrderStateMachine stateMachine,
            OutboxStore outboxStore,
            ErpRefundClient erpRefundClient,
            PackageRepository packageRepository,
            TmsInterceptClient tmsInterceptClient) {
        this.afterSaleRepository = afterSaleRepository;
        this.orderRepository = orderRepository;
        this.stateMachine = stateMachine;
        this.outboxStore = outboxStore;
        this.erpRefundClient = erpRefundClient;
        this.packageRepository = packageRepository;
        this.tmsInterceptClient = tmsInterceptClient;
    }

    public AfterSaleRecord applyReturn(String orderNo) {
        var order = orderRepository.findByOrderNo(orderNo).orElseThrow();
        if (order.getStatus().rank() < OrderStatus.SHIPPED.rank()) {
            throw new IllegalStateException("OMS_10030");
        }
        return afterSaleRepository.findByOrderNo(orderNo).orElseGet(() -> {
            AfterSaleRecord r = new AfterSaleRecord();
            r.setOrderNo(orderNo);
            r.setStatus("APPLIED");
            r.setRefundAmount(order.getPayAmount());
            return afterSaleRepository.save(r);
        });
    }

    public AfterSaleRecord approve(String afterSaleNo) {
        AfterSaleRecord r = afterSaleRepository.findByAfterSaleNo(afterSaleNo).orElseThrow();
        r.setStatus("APPROVED");
        afterSaleRepository.update(r);
        var order = orderRepository.findByOrderNo(r.getOrderNo()).orElseThrow();
        order.setStatus(stateMachine.apply(order.getStatus(), OrderStatus.AFTER_SALE));
        orderRepository.update(order);
        var packages = packageRepository.findAllByOrderNo(r.getOrderNo());
        if (packages.isEmpty()) {
            tmsInterceptClient.interceptPackages(List.of("P" + r.getOrderNo()));
        } else {
            tmsInterceptClient.interceptPackages(
                    packages.stream().map(p -> p.getPackageNo()).toList());
        }
        return r;
    }

    public AfterSaleRecord completeRefund(String orderNo) {
        AfterSaleRecord r = afterSaleRepository.findByOrderNo(orderNo).orElseThrow();
        r.setStatus("REFUND_SUCCESS");
        afterSaleRepository.update(r);
        String bizKey = "REFUND_COMPLETED+" + orderNo;
        if (!outboxStore.exists(bizKey)) {
            outboxStore.append(new OutboxEvent(
                    "REFUND_COMPLETED", bizKey, OffsetDateTime.now(),
                    "{\"order_no\":\"" + orderNo + "\",\"after_sale_no\":\"" + r.getAfterSaleNo()
                            + "\",\"amount\":\"" + r.getRefundAmount().toPlainString() + "\"}"
            ));
        }
        erpRefundClient.notifyRefundCompleted(bizKey, orderNo, r.getRefundAmount().toPlainString());
        return r;
    }

    public boolean hasRefundCompletedEvent(String orderNo) {
        String bizKey = "REFUND_COMPLETED+" + orderNo;
        return outboxStore.exists(bizKey) && outboxStore.isPublished(bizKey);
    }

    public String status(String orderNo) {
        return afterSaleRepository.findByOrderNo(orderNo)
                .map(AfterSaleRecord::getStatus)
                .orElse("NONE");
    }
}
