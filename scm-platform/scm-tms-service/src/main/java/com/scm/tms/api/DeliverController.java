package com.scm.tms.api;

import com.scm.tms.shipment.ShipmentApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/tms/v1/integration")
public class DeliverController {
    @Value("${oms.base-url:http://localhost:8081}")
    private String omsBaseUrl;

    private final RestTemplate rest = new RestTemplate();
    private final ShipmentApplicationService shipmentService;

    public DeliverController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/deliver")
    public Map<String, String> deliver(@RequestBody Map<String, String> body) {
        String packageNo = body.get("package_no");
        if (packageNo != null) {
            shipmentService.markDelivered(packageNo);
        }
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        rest.postForEntity(
                omsBaseUrl + "/api/v1/integration/tms/delivered",
                new HttpEntity<>(body, h),
                Map.class);
        return Map.of("status", "DELIVERED");
    }
}
