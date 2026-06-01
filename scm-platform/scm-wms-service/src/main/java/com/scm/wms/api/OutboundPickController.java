package com.scm.wms.api;

import com.scm.wms.outbound.OutboundShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/wms/v1/outbound")
public class OutboundPickController {

    private final OutboundShipmentService shipmentService;

    public OutboundPickController(OutboundShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/{outboundNo}/pick")
    public ResponseEntity<Map<String, String>> pick(@PathVariable String outboundNo) {
        shipmentService.confirmPick(outboundNo);
        return ResponseEntity.ok(Map.of("outbound_no", outboundNo, "status", "PICKED"));
    }

    @PostMapping("/{outboundNo}/check")
    public ResponseEntity<Map<String, String>> check(@PathVariable String outboundNo) {
        shipmentService.confirmCheck(outboundNo);
        return ResponseEntity.ok(Map.of("outbound_no", outboundNo, "status", "CHECKED"));
    }
}
