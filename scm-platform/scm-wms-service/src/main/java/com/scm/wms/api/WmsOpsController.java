package com.scm.wms.api;

import com.scm.common.web.ApiResponse;
import com.scm.wms.outbound.OutboundApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** E2E / 联调；生产通过 scm.ops.enabled=false 关闭 */
@RestController
@RequestMapping("/wms/v1/ops")
public class WmsOpsController {
    private final OutboundApplicationService outboundService;

    public WmsOpsController(OutboundApplicationService outboundService) {
        this.outboundService = outboundService;
    }

    @GetMapping("/outbound/count")
    public ApiResponse<Map<String, Object>> outboundCount(@RequestParam("package_no") String packageNo) {
        long count = outboundService.countByPackageNo(packageNo);
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "package_no", packageNo,
                "count", count
        ));
    }
}
