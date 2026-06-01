package com.scm.wms.api;

import com.scm.wms.outbound.OutboundApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/wms/v1")
public class OutboundController {
    private final OutboundApplicationService outboundService;

    public OutboundController(OutboundApplicationService outboundService) {
        this.outboundService = outboundService;
    }

    @PostMapping("/outbound/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody Map<String, Object> body) {
        OutboundApplicationService.CreateResult result = outboundService.create(key, body);
        if (result.conflict()) {
            return ResponseEntity.status(result.httpStatus()).body(Map.of(
                    "code", "WMS_10001",
                    "message", "Idempotent replay",
                    "outbound_no", result.outboundNo()
            ));
        }
        return ResponseEntity.status(result.httpStatus()).body(Map.of(
                "outbound_no", result.outboundNo(),
                "status", "CREATED"
        ));
    }
}
