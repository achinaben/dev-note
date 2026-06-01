package com.scm.oms.order;

import com.scm.oms.order.InMemoryOrderRepository;
import com.scm.oms.payment.InMemoryPaymentNotifyStore;
import com.scm.oms.inventory.InventoryTestSupport;
import com.scm.oms.outbox.InMemoryOutboxStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateOrderIdempotentTest {
    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new OrderApplicationService(
                new InMemoryOrderRepository(), new OrderStateMachine(),
                InventoryTestSupport.localInventory(), new InMemoryOutboxStore(), new InMemoryPaymentNotifyStore());
    }

    @Test
    void sameClientTokenReturnsSameOrder() {
        var line = List.of(Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01"));
        OrderRecord a = service.createOrder("U10001", "ct-fix-001", line).order();
        OrderRecord b = service.createOrder("U10001", "ct-fix-001", line).order();
        assertEquals(a.getOrderNo(), b.getOrderNo());
    }
}
