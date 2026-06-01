package com.scm.spring.event;



public final class ScmEventTopics {

    public static final String ORDER_LIFECYCLE = "scm.order.lifecycle";

    public static final String WMS_OUTBOUND = "scm.wms.outbound";



    private ScmEventTopics() {

    }



    public static String topicFor(String eventType) {

        return switch (eventType) {

            case "ORDER_PAID", "ORDER_CLOSED", "REFUND_COMPLETED" -> ORDER_LIFECYCLE;

            case "WMS_OUTBOUND_SHIPPED" -> WMS_OUTBOUND;

            default -> ORDER_LIFECYCLE;

        };

    }

}

