package com.scm.erp.api;

import com.scm.common.web.ApiResponse;
import com.scm.erp.integration.WmsShipmentPostingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration/wms")
public class IntegrationWmsController {
    private final WmsShipmentPostingService postingService;

    public IntegrationWmsController(WmsShipmentPostingService postingService) {
        this.postingService = postingService;
    }

    @PostMapping("/shipment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> postShipment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody ShipmentPostRequest body) {
        String requestId = UUID.randomUUID().toString();
        String bizKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : "WMS_OUTBOUND_SHIPPED+" + body.outboundNo();
        var req = new WmsShipmentPostingService.WmsShipmentRequest(
                body.outboundNo(),
                body.sourceOrderNo(),
                body.orgId(),
                body.whCode(),
                body.lines()
        );
        var result = postingService.postShipment(bizKey, req);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("erp_txn_no", result.erpTxnNo());
        data.put("je_no", result.jeNo());
        data.put("posting_status", "POSTED");
        if (result.idempotentHit()) {
            data.put("idempotent", true);
            return ResponseEntity.ok(new ApiResponse<>("ERP_02001", "Idempotent hit", requestId, data));
        }
        return ResponseEntity.ok(ApiResponse.ok(requestId, data));
    }

    public record ShipmentPostRequest(
            String outboundNo,
            String sourceSystem,
            String sourceOrderNo,
            String orgId,
            String whCode,
            String shippedAt,
            List<Map<String, String>> lines
    ) {
    }
}
