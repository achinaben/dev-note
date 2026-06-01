package com.scm.oms.event;

import com.scm.common.event.EventEnvelope;
import com.scm.oms.fulfillment.FulfillmentService;
import com.scm.spring.event.EventDispatcher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class WmsOutboundShippedHandler {
    private final EventDispatcher dispatcher;
    private final FulfillmentService fulfillmentService;

    public WmsOutboundShippedHandler(EventDispatcher dispatcher, FulfillmentService fulfillmentService) {
        this.dispatcher = dispatcher;
        this.fulfillmentService = fulfillmentService;
    }

    @PostConstruct
    void register() {
        dispatcher.subscribe("WMS_OUTBOUND_SHIPPED", this::onShipped);
    }

    private void onShipped(EventEnvelope envelope) {
        var data = envelope.data();
        String orderNo = data.path("source_order_no").asText();
        String outboundNo = data.path("outbound_no").asText();
        String packageNo = data.has("package_no")
                ? data.get("package_no").asText()
                : "P" + orderNo;
        String waybillNo = data.has("waybill_no") ? data.get("waybill_no").asText() : null;
        fulfillmentService.applyWmsShippedEvent(orderNo, packageNo, outboundNo, waybillNo);
    }
}
