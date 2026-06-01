package com.scm.tms.api;

import com.scm.common.web.ApiResponse;
import com.scm.tms.shipment.ShipmentApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tms/v1")
public class ShipmentController {
    private final ShipmentApplicationService shipmentService;

    public ShipmentController(ShipmentApplicationService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/shipment/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody Map<String, Object> body) {
        ShipmentApplicationService.CreateResult result = shipmentService.create(body);
        if (result.conflict()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("TMS_10001", "Idempotent replay",
                            UUID.randomUUID().toString(), result.data()));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(UUID.randomUUID().toString(), result.data()));
    }
}
