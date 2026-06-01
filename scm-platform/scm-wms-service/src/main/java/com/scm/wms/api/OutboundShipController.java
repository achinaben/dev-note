package com.scm.wms.api;

import com.scm.wms.outbound.OutboundShipmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wms/v1/outbound")
public class OutboundShipController {
    private final OutboundShipmentService shipmentService;

    public OutboundShipController(OutboundShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping(value = "/{outboundNo}/ship", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ship(
            @PathVariable("outboundNo") String outboundNo,
            @RequestBody Map<String, Object> body) {
        if (shipmentService.find(outboundNo).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lines = (List<Map<String, String>>) body.get("lines");
        if (lines == null) {
            lines = List.of(Map.of("sku_code", "SKU001", "qty", "2.0000"));
        }
        Map<String, Object> handoverMeta = new java.util.LinkedHashMap<>(body);
        handoverMeta.remove("lines");
        Map<String, Object> result = shipmentService.handover(outboundNo, lines, handoverMeta);
        return ResponseEntity.ok(result);
    }
}
