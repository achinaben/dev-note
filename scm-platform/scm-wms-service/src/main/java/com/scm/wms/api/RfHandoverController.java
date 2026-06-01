package com.scm.wms.api;

import com.scm.wms.outbound.OutboundShipmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rf/v1/ship")
public class RfHandoverController {

    private final OutboundShipmentService shipmentService;

    public RfHandoverController(OutboundShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping(value = "/handover", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handover(@RequestBody Map<String, Object> body) {
        String outboundNo = String.valueOf(body.get("outbound_no"));
        List<Map<String, String>> lines = List.of(Map.of("sku_code", "SKU001", "qty", "2.0000"));
        Map<String, Object> result = shipmentService.handover(outboundNo, lines, body);
        return ResponseEntity.ok(result);
    }
}
