package com.scm.erp.api;

import com.scm.common.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MaterialController {
    @GetMapping("/materials/{code}")
    public ApiResponse<Map<String, Object>> get(@PathVariable("code") String code) {
        if (!"M001".equals(code)) {
            throw new IllegalArgumentException("ERP_02002");
        }
        return ApiResponse.ok(UUID.randomUUID().toString(), Map.of(
                "material_code", code,
                "material_name", "测试商品物料",
                "uom_code", "EA"
        ));
    }
}
