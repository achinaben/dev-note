package com.scm.oms.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {
    private final OrderStateMachine sm = new OrderStateMachine();

    @ParameterizedTest
    @CsvSource({
            "CREATED,PAID,true",
            "PAID,SHIPPED,true",
            "SHIPPED,DELIVERED,true",
            "DELIVERED,CREATED,false",
            "SHIPPED,PAID,false"
    })
    void transitions(String from, String to, boolean allowed) {
        OrderStatus f = OrderStatus.valueOf(from);
        OrderStatus t = OrderStatus.valueOf(to);
        assertEquals(allowed, sm.canTransition(f, t));
    }

    @Test
    void applyRejectsBackward() {
        assertThrows(IllegalStateException.class,
                () -> sm.apply(OrderStatus.DELIVERED, OrderStatus.FULFILLING));
    }
}
