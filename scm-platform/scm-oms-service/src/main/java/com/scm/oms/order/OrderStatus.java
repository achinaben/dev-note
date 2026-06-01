package com.scm.oms.order;

public enum OrderStatus {
    CREATED(10),
    CLOSED(5),
    PAID(20),
    FULFILLING(30),
    PARTIAL_SHIPPED(35),
    SHIPPED(40),
    DELIVERED(50),
    COMPLETED(60),
    CANCELLED(8),
    AFTER_SALE(65);

    private final int rank;

    OrderStatus(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
