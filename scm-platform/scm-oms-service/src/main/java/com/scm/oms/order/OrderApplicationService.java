package com.scm.oms.order;

import com.scm.common.tenant.OrgIdContext;
import com.scm.oms.fulfillment.FulfillmentService;
import com.scm.oms.inventory.InventoryService;
import com.scm.oms.outbox.OutboxEvent;
import com.scm.oms.outbox.OutboxStore;
import com.scm.oms.payment.PaymentNotifyStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderApplicationService {
    private final OrderRepository orderRepository;
    private final OrderStateMachine stateMachine;
    private final InventoryService inventoryService;
    private final OutboxStore outboxStore;
    private final PaymentNotifyStore paymentNotifyStore;

    public OrderApplicationService(
            OrderRepository orderRepository,
            OrderStateMachine stateMachine,
            InventoryService inventoryService,
            OutboxStore outboxStore,
            PaymentNotifyStore paymentNotifyStore) {
        this.orderRepository = orderRepository;
        this.stateMachine = stateMachine;
        this.inventoryService = inventoryService;
        this.outboxStore = outboxStore;
        this.paymentNotifyStore = paymentNotifyStore;
    }

    public CreateOrderResult createOrder(String buyerId, String clientToken, List<Map<String, String>> lines) {
        var existing = orderRepository.findByClientToken(buyerId, clientToken);
        if (existing.isPresent()) {
            return new CreateOrderResult(existing.get(), false);
        }
        OrderRecord o = new OrderRecord();
        o.setBuyerId(buyerId);
        o.setClientToken(clientToken);
        o.setStatus(OrderStatus.CREATED);
        o.setPayAmount(new BigDecimal("199.0000"));
        o.setVersion(0);
        OrderRecord saved = orderRepository.saveNew(o);
        inventoryService.reserve(saved.getOrderNo());
        return new CreateOrderResult(saved, true);
    }

    public record CreateOrderResult(OrderRecord order, boolean created) {
    }

    public OrderRecord markPaid(String orderNo, String notifyId) {
        OrderRecord o = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("OMS_10002"));
        if (o.getStatus() == OrderStatus.PAID) {
            return o;
        }
        o.setStatus(stateMachine.apply(o.getStatus(), OrderStatus.PAID));
        o.setPayTime(OffsetDateTime.now());
        orderRepository.update(o);
        inventoryService.confirm(orderNo);
        String bizKey = "ORDER_PAID+" + orderNo;
        if (!outboxStore.exists(bizKey)) {
            String orgId = OrgIdContext.get();
            outboxStore.append(new OutboxEvent(
                    "ORDER_PAID", bizKey, o.getPayTime(),
                    "{\"order_no\":\"" + orderNo + "\",\"org_id\":\"" + orgId + "\"}"
            ));
        }
        return o;
    }

    public boolean hasOrderPaidEvent(String orderNo) {
        String bizKey = "ORDER_PAID+" + orderNo;
        return outboxStore.exists(bizKey) && outboxStore.isPublished(bizKey);
    }

    public OrderRecord closeUnpaid(String orderNo) {
        OrderRecord o = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("OMS_10002"));
        if (o.getStatus() == OrderStatus.CLOSED) {
            return o;
        }
        if (o.getStatus().rank() >= OrderStatus.PAID.rank()) {
            throw new IllegalStateException("OMS_10010");
        }
        if (o.getStatus() != OrderStatus.CREATED && o.getStatus() != OrderStatus.CANCELLED) {
            throw new IllegalStateException("OMS_10010");
        }
        o.setStatus(OrderStatus.CLOSED);
        orderRepository.update(o);
        inventoryService.release(orderNo);
        return o;
    }

    public String inventoryStatus(String orderNo) {
        return inventoryService.status(orderNo);
    }

    public OrderRecord get(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("OMS_10002"));
    }

    public long countTradeOrders(String buyerId, String clientToken) {
        return orderRepository.countByBuyerAndToken(buyerId, clientToken);
    }
}
