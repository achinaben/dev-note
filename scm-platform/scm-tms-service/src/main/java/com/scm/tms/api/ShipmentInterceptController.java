package com.scm.tms.api;

import com.scm.common.web.ApiResponse;
import com.scm.tms.shipment.ShipmentApplicationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tms/v1/shipment")
public class ShipmentInterceptController {
    private final ShipmentApplicationService shipmentService;

    public ShipmentInterceptController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/{shipmentNo}/intercept")
    public ApiResponse<Map<String, Object>> intercept(@PathVariable("shipmentNo") String shipmentNo) {
        return ApiResponse.ok(UUID.randomUUID().toString(), shipmentService.intercept(shipmentNo));
    }
}
