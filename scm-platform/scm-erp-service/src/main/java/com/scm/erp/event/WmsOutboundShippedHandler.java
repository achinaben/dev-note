package com.scm.erp.event;

import com.scm.common.event.EventEnvelope;
import com.scm.erp.integration.WmsShipmentPostingService;
import com.scm.spring.event.EventDispatcher;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WmsOutboundShippedHandler {
    private final EventDispatcher dispatcher;
    private final WmsShipmentPostingService postingService;

    public WmsOutboundShippedHandler(EventDispatcher dispatcher, WmsShipmentPostingService postingService) {
        this.dispatcher = dispatcher;
        this.postingService = postingService;
    }

    @PostConstruct
    void register() {
        dispatcher.subscribe("WMS_OUTBOUND_SHIPPED", this::onShipped);
    }

    private void onShipped(EventEnvelope envelope) {
        var data = envelope.data();
        String outboundNo = data.get("outbound_no").asText();
        String sourceOrderNo = data.has("source_order_no")
                ? data.get("source_order_no").asText()
                : "O-unknown";
        String orgId = data.has("org_id") ? data.get("org_id").asText() : "ORG001";
        String whCode = data.has("wh_code") ? data.get("wh_code").asText() : "WH-SH-01";
        List<Map<String, String>> lines = List.of(Map.of(
                "material_code", "M001",
                "qty", "2.0000"
        ));
        String waybillNo = data.has("waybill_no") ? data.get("waybill_no").asText() : null;
        postingService.postShipment(envelope.bizKey(), new WmsShipmentPostingService.WmsShipmentRequest(
                outboundNo, sourceOrderNo, orgId, whCode, lines, waybillNo
        ));
    }
}
