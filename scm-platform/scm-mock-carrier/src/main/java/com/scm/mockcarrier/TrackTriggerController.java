package com.scm.mockcarrier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 联调：模拟承运商轨迹回调 TMS（类似 mock-pay 触发支付）。 */
@RestController
public class TrackTriggerController {
    @Value("${tms.callback-base-url:http://localhost:8083/tms/v1/integration/carrier}")
    private String callbackBaseUrl;

    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/trigger/track")
    public String trigger(@RequestBody Map<String, String> body) {
        String waybill = body.getOrDefault("waybill_no", "SF-FIX-001");
        String status = body.getOrDefault("carrier_status", "IN_TRANSIT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("waybill_no", waybill);
        payload.put("carrier_status", status);
        payload.put("event_time", OffsetDateTime.now().toString());
        payload.put("location", body.getOrDefault("location", "杭州"));
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        String carrier = body.getOrDefault("carrier_code", inferCarrier(waybill));
        String callbackUrl = callbackBaseUrl + "/" + carrier + "/callback";
        rest.postForEntity(callbackUrl, new HttpEntity<>(payload, h), String.class);
        return "OK";
    }

    private static String inferCarrier(String waybill) {
        if (waybill != null && waybill.contains("-")) {
            return waybill.substring(0, waybill.indexOf('-'));
        }
        return "SF";
    }
}
