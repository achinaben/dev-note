package com.scm.oms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TmsFreightClient {
    @Value("${tms.base-url:http://localhost:8083}")
    private String tmsBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String recommendedCarrierCode() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_warehouse_code", "WH-SH-01");
        body.put("receiver", Map.of("city", "杭州市"));
        body.put("packages", List.of(Map.of("weight_kg", "1.2")));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = rest.exchange(
                tmsBaseUrl + "/tms/v1/freight/estimate",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {});
        if (response.getBody() == null) {
            return "SF";
        }
        Object data = response.getBody().get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            return "SF";
        }
        Object recommended = dataMap.get("recommended");
        if (recommended instanceof Map<?, ?> rec) {
            Object code = rec.get("carrier_code");
            if (code instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return "SF";
    }
}
