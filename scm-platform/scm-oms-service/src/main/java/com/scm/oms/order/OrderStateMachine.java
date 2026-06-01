package com.scm.oms.order;

import org.springframework.stereotype.Component;

@Component
public class OrderStateMachine {

    public boolean canTransition(OrderStatus current, OrderStatus next) {
        if (next.rank() < current.rank()) {
            return false;
        }
        return next.rank() >= current.rank();
    }

    public OrderStatus apply(OrderStatus current, OrderStatus next) {
        if (!canTransition(current, next)) {
            throw new IllegalStateException(
                    "Cannot transition from " + current + " to " + next);
        }
        return next;
    }
}
