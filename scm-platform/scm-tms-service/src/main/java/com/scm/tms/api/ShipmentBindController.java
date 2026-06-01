package com.scm.tms.api;

import com.scm.common.web.ApiResponse;
import com.scm.tms.shipment.ShipmentApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tms/v1/shipment")
public class ShipmentBindController {

    private final ShipmentApplicationService shipmentService;

    public ShipmentBindController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/bind-waybill")
    public ApiResponse<Map<String, Object>> bindWaybill(@RequestBody Map<String, String> body) {
        Map<String, Object> data = shipmentService.bindWaybillFromHandover(
                body.get("package_no"),
                body.getOrDefault("order_no", ""),
                body.get("waybill_no"));
        return ApiResponse.ok(UUID.randomUUID().toString(), data);
    }
}
