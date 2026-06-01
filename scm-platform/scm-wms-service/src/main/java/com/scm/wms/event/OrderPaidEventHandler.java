package com.scm.wms.event;

import com.scm.common.event.EventEnvelope;
import com.scm.spring.event.EventDispatcher;
import com.scm.wms.outbound.OutboundApplicationService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class OrderPaidEventHandler {
    private final EventDispatcher dispatcher;
    private final OutboundApplicationService outboundService;

    public OrderPaidEventHandler(EventDispatcher dispatcher, OutboundApplicationService outboundService) {
        this.dispatcher = dispatcher;
        this.outboundService = outboundService;
    }

    @PostConstruct
    void register() {
        dispatcher.subscribe("ORDER_PAID", this::onOrderPaid);
    }

    private void onOrderPaid(EventEnvelope envelope) {
        String orderNo = envelope.data().get("order_no").asText();
        outboundService.createForOrderPaid(orderNo);
    }
}
