package com.scm.mockpay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class TriggerController {
    @Value("${oms.notify-url:http://localhost:8081/api/v1/payments/notify/wechat}")
    private String notifyUrl;

    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/trigger")
    public String trigger(@RequestBody Map<String, String> body) {
        String orderNo = body.get("order_no");
        if (orderNo == null) {
            orderNo = body.get("orderNo");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notify_id", body.getOrDefault("notify_id",
                body.getOrDefault("notifyId", "notify-mock-" + orderNo)));
        payload.put("order_no", orderNo);
        payload.put("out_trade_no", body.getOrDefault("out_trade_no",
                body.getOrDefault("outTradeNo", "PAY-" + orderNo)));
        payload.put("amount_minor", 19900);
        payload.put("sign_verified", true);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.postForEntity(notifyUrl, new HttpEntity<>(payload, h), String.class);
        return "OK";
    }
}
