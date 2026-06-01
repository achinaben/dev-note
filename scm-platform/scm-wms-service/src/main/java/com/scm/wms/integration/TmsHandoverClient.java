package com.scm.wms.integration;

import com.scm.wms.config.IntegrationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TmsHandoverClient {

    @Value("${tms.base-url:http://localhost:8083}")
    private String tmsBaseUrl;

    private final RestTemplate rest = new RestTemplate();
    private final IntegrationProperties integrationProperties;

    public TmsHandoverClient(IntegrationProperties integrationProperties) {
        this.integrationProperties = integrationProperties;
    }

    public void bindWaybillIfPresent(String packageNo, String orderNo, Map<String, Object> handoverMeta) {
        if (!integrationProperties.tmsHandoverOn()) {
            return;
        }
        Object wb = handoverMeta.get("waybill_no");
        if (wb == null || String.valueOf(wb).isBlank()) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
                "package_no", packageNo,
                "order_no", orderNo == null ? "" : orderNo,
                "waybill_no", String.valueOf(wb));
        rest.postForEntity(
                tmsBaseUrl + "/tms/v1/shipment/bind-waybill",
                new HttpEntity<>(body, headers),
                Map.class);
    }
}
