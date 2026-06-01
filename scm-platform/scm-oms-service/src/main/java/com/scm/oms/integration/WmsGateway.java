package com.scm.oms.integration;

import java.util.Optional;

public interface WmsGateway {
    String createOutbound(String packageNo, String orderNo);

    default Optional<String> findOutboundByOrder(String orderNo) {
        return Optional.empty();
    }

    default void ship(String outboundNo) {
        ship(outboundNo, null);
    }

    void ship(String outboundNo, String waybillNo);
}
