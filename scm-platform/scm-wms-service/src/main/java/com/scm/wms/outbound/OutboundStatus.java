package com.scm.wms.outbound;

public enum OutboundStatus {
    CREATED(0),
    PICKED(1),
    CHECKED(2),
    SHIPPED(3);

    private final int rank;

    OutboundStatus(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public static OutboundStatus parse(String status) {
        return OutboundStatus.valueOf(status);
    }
}
