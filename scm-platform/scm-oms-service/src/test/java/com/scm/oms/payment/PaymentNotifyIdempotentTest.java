package com.scm.oms.payment;

import com.scm.oms.inventory.InventoryTestSupport;
import com.scm.oms.order.OrderApplicationService;
import com.scm.oms.order.OrderRepository;
import com.scm.oms.order.OrderStateMachine;
import com.scm.oms.order.OrderStatus;
import com.scm.oms.order.InMemoryOrderRepository;
import com.scm.oms.outbox.InMemoryOutboxStore;
import com.scm.oms.payment.InMemoryPaymentNotifyStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentNotifyIdempotentTest {
    private OrderApplicationService service;
    private InMemoryOutboxStore outboxStore;

    @BeforeEach
    void setUp() {
        outboxStore = new InMemoryOutboxStore();
        service = new OrderApplicationService(
                new InMemoryOrderRepository(), new OrderStateMachine(),
                InventoryTestSupport.localInventory(), outboxStore, new InMemoryPaymentNotifyStore());
    }

    @Test
    void payIdempotentAndOutboxOnce() {
        var line = List.of(Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01"));
        String orderNo = service.createOrder("U10001", "ct-pay-004", line).order().getOrderNo();
        service.markPaid(orderNo, "notify-1");
        service.markPaid(orderNo, "notify-1");
        outboxStore.markPublished("ORDER_PAID+" + orderNo);
        assertEquals(OrderStatus.PAID, service.get(orderNo).getStatus());
        assertTrue(service.hasOrderPaidEvent(orderNo));
        assertEquals("CONFIRMED", service.inventoryStatus(orderNo));
    }
}
