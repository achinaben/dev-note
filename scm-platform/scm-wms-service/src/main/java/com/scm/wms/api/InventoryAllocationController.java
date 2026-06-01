package com.scm.wms.api;

import com.scm.wms.inventory.InventoryAllocationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/inventory/v1")
public class InventoryAllocationController {

    private final InventoryAllocationService allocationService;

    public InventoryAllocationController(InventoryAllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/reserve")
    public Map<String, Object> reserve(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody Map<String, Object> body) {
        return allocationService.reserve(key, body);
    }

    @PostMapping("/confirm")
    public Map<String, Object> confirm(@RequestBody Map<String, String> body) {
        return allocationService.confirm(body);
    }

    @PostMapping("/release")
    public Map<String, Object> release(@RequestBody Map<String, String> body) {
        return allocationService.release(body);
    }

    @PostMapping("/status")
    public Map<String, Object> status(@RequestBody Map<String, String> body) {
        return allocationService.status(body.get("order_no"));
    }
}
