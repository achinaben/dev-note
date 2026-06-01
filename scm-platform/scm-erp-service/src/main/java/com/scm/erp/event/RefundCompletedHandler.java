package com.scm.erp.event;

import com.scm.common.event.EventEnvelope;
import com.scm.erp.integration.RefundPostingService;
import com.scm.spring.event.EventDispatcher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RefundCompletedHandler {
    private final EventDispatcher dispatcher;
    private final RefundPostingService refundPostingService;

    public RefundCompletedHandler(EventDispatcher dispatcher, RefundPostingService refundPostingService) {
        this.dispatcher = dispatcher;
        this.refundPostingService = refundPostingService;
    }

    @PostConstruct
    void register() {
        dispatcher.subscribe("REFUND_COMPLETED", this::onRefund);
    }

    private void onRefund(EventEnvelope envelope) {
        String orderNo = envelope.data().get("order_no").asText();
        String amount = envelope.data().has("amount")
                ? envelope.data().get("amount").asText()
                : "0";
        refundPostingService.postRefund(envelope.bizKey(), orderNo, amount);
    }
}
