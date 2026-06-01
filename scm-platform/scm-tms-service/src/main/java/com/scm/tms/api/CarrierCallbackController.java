package com.scm.tms.api;

import com.scm.common.web.ApiResponse;
import com.scm.tms.shipment.ShipmentApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tms/v1/integration/carrier")
public class CarrierCallbackController {
    @Value("${oms.base-url:http://localhost:8081}")
    private String omsBaseUrl;

    private final RestTemplate rest = new RestTemplate();
    private final ShipmentApplicationService shipmentService;

    public CarrierCallbackController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/{carrierCode}/callback")
    public ApiResponse<Map<String, String>> callback(
            @PathVariable("carrierCode") String carrierCode,
            @RequestBody Map<String, Object> body) {
        String waybillNo = (String) body.get("waybill_no");
        String carrierStatus = (String) body.get("carrier_status");
        var shipment = shipmentService.findByWaybillNo(waybillNo)
                .orElseThrow(() -> new IllegalArgumentException("TMS_10010"));
        String omsEvent = shipmentService.mapCarrierStatusToOmsEvent(carrierStatus);
        if (omsEvent != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.postForEntity(
                    omsBaseUrl + "/api/v1/integration/tms/track",
                    new HttpEntity<>(Map.of("order_no", shipment.getOrderNo(), "event", omsEvent), headers),
                    Map.class);
            if ("TMS_DELIVERED".equals(omsEvent)) {
                shipmentService.markDelivered(shipment.getPackageNo());
            }
        }
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "carrier_code", carrierCode,
                "waybill_no", waybillNo,
                "forwarded_event", omsEvent == null ? "" : omsEvent
        ));
    }
}
