package com.scm.tms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class HttpCarrierGateway implements CarrierGateway {
    @Value("${carrier.base-url:http://localhost:8089}")
    private String carrierBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> quote(Map<String, Object> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var response = rest.exchange(
                carrierBaseUrl + "/carrier/v1/quote",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {});
        Object options = response.getBody() != null ? response.getBody().get("options") : null;
        if (options instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }
}
