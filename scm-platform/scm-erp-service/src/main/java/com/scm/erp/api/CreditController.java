package com.scm.erp.api;

import com.scm.common.web.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {
    private static final BigDecimal LIMIT = new BigDecimal("500000.0000");
    private static final BigDecimal USED = new BigDecimal("120000.0000");

    @PostMapping("/check")
    public ApiResponse<Map<String, Object>> check(@RequestBody CreditCheckRequest req) {
        BigDecimal order = new BigDecimal(req.orderAmount());
        BigDecimal available = LIMIT.subtract(USED);
        boolean allowed = order.compareTo(available) <= 0;
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "allowed", allowed,
                "credit_limit", LIMIT.toPlainString(),
                "credit_used", USED.toPlainString(),
                "credit_available", available.toPlainString()
        ));
    }

    public record CreditCheckRequest(
            String partnerId,
            String orgId,
            String currency,
            String orderAmount
    ) {
    }
}
