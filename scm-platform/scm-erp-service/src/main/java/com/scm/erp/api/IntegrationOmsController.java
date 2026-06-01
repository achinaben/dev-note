package com.scm.erp.api;

import com.scm.common.web.ApiResponse;
import com.scm.erp.integration.RefundPostingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration/oms")
public class IntegrationOmsController {
    private final RefundPostingService refundPostingService;

    public IntegrationOmsController(RefundPostingService refundPostingService) {
        this.refundPostingService = refundPostingService;
    }

    @PostMapping("/refund-completed")
    public ApiResponse<Map<String, String>> refundCompleted(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody RefundRequest body) {
        String bizKey = idempotencyKey != null && !idempotencyKey.isBlank()
                ? idempotencyKey
                : "REFUND_COMPLETED+" + body.orderNo();
        var result = refundPostingService.postRefund(bizKey, body.orderNo(), body.amount());
        if (result.idempotentHit()) {
            return new ApiResponse<>("ERP_02001", "Idempotent replay",
                    UUID.randomUUID().toString(), Map.of("je_no", result.jeNo()));
        }
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of("je_no", result.jeNo()));
    }

    public record RefundRequest(String orderNo, String amount) {
    }
}
