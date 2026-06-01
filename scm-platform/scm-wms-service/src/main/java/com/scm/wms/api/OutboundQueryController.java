package com.scm.wms.api;

import com.scm.wms.outbound.OutboundApplicationService;
import com.scm.wms.outbound.OutboundShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/wms/v1/outbound")
public class OutboundQueryController {
    private final OutboundApplicationService outboundService;
    private final OutboundShipmentService shipmentService;

    public OutboundQueryController(
            OutboundApplicationService outboundService,
            OutboundShipmentService shipmentService) {
        this.outboundService = outboundService;
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{outboundNo}")
    public ResponseEntity<Map<String, String>> detail(@PathVariable("outboundNo") String outboundNo) {
        return shipmentService.find(outboundNo)
                .map(r -> ResponseEntity.ok(Map.of(
                        "outbound_no", r.getOutboundNo(),
                        "package_no", r.getPackageNo(),
                        "order_no", r.getSourceOrderNo(),
                        "status", r.getStatus())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-order/{orderNo}")
    public ResponseEntity<Map<String, String>> byOrder(@PathVariable("orderNo") String orderNo) {
        return outboundService.findOutboundNoByOrder(orderNo)
                .map(ob -> ResponseEntity.ok(Map.of("outbound_no", ob, "order_no", orderNo)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-package/{packageNo}")
    public ResponseEntity<Map<String, String>> byPackage(@PathVariable("packageNo") String packageNo) {
        return outboundService.findByPackageNo(packageNo)
                .map(r -> ResponseEntity.ok(Map.of(
                        "outbound_no", r.getOutboundNo(),
                        "package_no", r.getPackageNo(),
                        "order_no", r.getSourceOrderNo())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
