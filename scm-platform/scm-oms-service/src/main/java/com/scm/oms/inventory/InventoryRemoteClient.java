package com.scm.oms.inventory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class InventoryRemoteClient {

    private final InventorySettings settings;
    private final RestTemplate rest = new RestTemplate();

    public InventoryRemoteClient(InventorySettings settings) {
        this.settings = settings;
    }

    public void reserve(String orderNo) {
        String base = settings.resolveBaseUrl();
        Map<String, Object> body = Map.of(
                "client_token", "inv-" + orderNo,
                "order_no", orderNo,
                "warehouse_id", "WH-SH-01",
                "lines", List.of(Map.of("sku_id", "SKU001", "qty", "2"))
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", orderNo);
        rest.postForEntity(base + "/reserve", new HttpEntity<>(body, headers), Map.class);
    }

    public void confirm(String orderNo) {
        postJson("/confirm", Map.of("order_no", orderNo, "reserve_id", "RSV-" + orderNo));
    }

    public void release(String orderNo) {
        postJson("/release", Map.of("order_no", orderNo, "reason", "TIMEOUT"));
    }

    private void postJson(String path, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.postForEntity(settings.resolveBaseUrl() + path, new HttpEntity<>(body, headers), Map.class);
    }
}
