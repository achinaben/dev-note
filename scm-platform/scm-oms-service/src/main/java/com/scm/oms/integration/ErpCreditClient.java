package com.scm.oms.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ErpCreditClient {
    @Value("${erp.base-url:http://localhost:8084}")
    private String erpBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    @SuppressWarnings("unchecked")
    public boolean checkCredit(String partnerId, String orderAmount) {
        Map<String, Object> body = Map.of(
                "partner_id", partnerId,
                "org_id", "ORG001",
                "currency", "CNY",
                "order_amount", orderAmount
        );
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = rest.postForObject(
                erpBaseUrl + "/api/v1/credit/check", body, Map.class);
        if (resp == null) {
            return false;
        }
        Object data = resp.get("data");
        if (data instanceof Map<?, ?> map) {
            return Boolean.TRUE.equals(map.get("allowed"));
        }
        return false;
    }
}
