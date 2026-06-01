package com.scm.oms.api;

import com.scm.common.web.ApiResponse;
import com.scm.oms.fulfillment.FulfillmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration")
public class IntegrationCallbackController {
    private final FulfillmentService fulfillmentService;

    public IntegrationCallbackController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @PostMapping("/tms/delivered")
    public ApiResponse<Void> tmsDelivered(@RequestBody Map<String, String> body) {
        fulfillmentService.markDelivered(body.get("order_no"));
        return ApiResponse.ok(UUID.randomUUID().toString(), null);
    }
}
