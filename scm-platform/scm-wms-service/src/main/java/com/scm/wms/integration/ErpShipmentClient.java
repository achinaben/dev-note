package com.scm.wms.integration;

import com.scm.common.tenant.OrgIdContext;
import com.scm.spring.event.EventEnvelopeFactory;
import com.scm.spring.event.ScmEventPublisher;
import com.scm.wms.config.IntegrationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ErpShipmentClient {
    @Value("${erp.base-url:http://localhost:8084}")
    private String erpBaseUrl;

    private final RestTemplate rest = new RestTemplate();
    private final ScmEventPublisher eventPublisher;
    private final IntegrationProperties integrationProperties;

    public ErpShipmentClient(ScmEventPublisher eventPublisher, IntegrationProperties integrationProperties) {
        this.eventPublisher = eventPublisher;
        this.integrationProperties = integrationProperties;
    }

    public void notifyOutboundShipped(
            String outboundNo,
            String sourceOrderNo,
            String packageNo,
            List<Map<String, String>> lines) {
        notifyOutboundShipped(outboundNo, sourceOrderNo, packageNo, lines, Map.of());
    }

    public void notifyOutboundShipped(
            String outboundNo,
            String sourceOrderNo,
            String packageNo,
            List<Map<String, String>> lines,
            Map<String, Object> handoverMeta) {
        String bizKey = "WMS_OUTBOUND_SHIPPED+" + outboundNo;
        String orgId = OrgIdContext.get();
        String waybill = String.valueOf(handoverMeta.getOrDefault("waybill_no", ""));
        String weight = String.valueOf(handoverMeta.getOrDefault("weight_kg", ""));
        if (integrationProperties.useOff()) {
            return;
        }
        if (integrationProperties.useKafka()) {
            publishShipHandoverEvent(outboundNo, sourceOrderNo, packageNo, waybill, weight);
            String payload = """
                    {"outbound_no":"%s","source_order_no":"%s","org_id":"%s","wh_code":"WH-SH-01",
                     "package_no":"%s","shipped_at":"%s","waybill_no":"%s","weight_kg":"%s",
                     "lines":[{"sku_code":"SKU001","qty":"2"}]}
                    """.formatted(
                    outboundNo, sourceOrderNo, orgId, packageNo, OffsetDateTime.now(), waybill, weight);
            eventPublisher.publish(EventEnvelopeFactory.of(
                    "WMS_OUTBOUND_SHIPPED", bizKey, "wms", payload));
        }
        if (integrationProperties.useHttp()) {
            postHttp(bizKey, outboundNo, sourceOrderNo, lines);
        }
    }

    private void publishShipHandoverEvent(
            String outboundNo, String sourceOrderNo, String packageNo, String waybill, String weight) {
        String handoverKey = "WMS_SHIP_HANDOVER+" + outboundNo;
        String payload = """
                {"outbound_no":"%s","source_order_no":"%s","package_no":"%s",
                 "waybill_no":"%s","weight_kg":"%s","handover_at":"%s"}
                """.formatted(outboundNo, sourceOrderNo, packageNo, waybill, weight, OffsetDateTime.now());
        eventPublisher.publish(EventEnvelopeFactory.of(
                "WMS_SHIP_HANDOVER", handoverKey, "wms", payload));
    }

    private void postHttp(
            String bizKey,
            String outboundNo,
            String sourceOrderNo,
            List<Map<String, String>> lines) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("outbound_no", outboundNo);
        body.put("source_system", "WMS");
        body.put("source_order_no", sourceOrderNo);
        body.put("org_id", OrgIdContext.get());
        body.put("wh_code", "WH-SH-01");
        body.put("shipped_at", OffsetDateTime.now().toString());
        body.put("lines", lines.stream().map(l -> Map.of(
                "material_code", "M001",
                "qty", l.getOrDefault("qty", "1")
        )).toList());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", bizKey);
        rest.postForEntity(
                erpBaseUrl + "/api/v1/integration/wms/shipment",
                new HttpEntity<>(body, headers),
                Map.class);
    }
}
