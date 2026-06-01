package com.scm.oms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ErpRefundClient {
    @Value("${erp.base-url:http://localhost:8084}")
    private String erpBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    public void notifyRefundCompleted(String bizKey, String orderNo, String amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", bizKey);
        rest.postForEntity(
                erpBaseUrl + "/api/v1/integration/oms/refund-completed",
                new HttpEntity<>(Map.of("orderNo", orderNo, "amount", amount), headers),
                Map.class);
    }
}
