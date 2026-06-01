package com.scm.oms.api;

import com.scm.common.web.ApiResponse;
import com.scm.oms.aftersale.AfterSaleRecord;
import com.scm.oms.aftersale.AfterSaleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/after-sale")
public class AfterSaleController {
    private final AfterSaleService afterSaleService;

    public AfterSaleController(AfterSaleService afterSaleService) {
        this.afterSaleService = afterSaleService;
    }

    @PostMapping("/apply")
    public ApiResponse<Map<String, String>> apply(@RequestBody Map<String, String> body) {
        AfterSaleRecord r = afterSaleService.applyReturn(body.get("order_no"));
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "after_sale_no", r.getAfterSaleNo(),
                "status", r.getStatus()
        ));
    }
}
