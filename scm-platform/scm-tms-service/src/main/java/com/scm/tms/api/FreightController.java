package com.scm.tms.api;

import com.scm.common.web.ApiResponse;
import com.scm.tms.freight.FreightApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tms/v1")
public class FreightController {
    private final FreightApplicationService freightService;

    public FreightController(FreightApplicationService freightService) {
        this.freightService = freightService;
    }

    @PostMapping("/freight/estimate")
    public ApiResponse<Map<String, Object>> estimate(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(UUID.randomUUID().toString(), freightService.estimate(body));
    }
}
