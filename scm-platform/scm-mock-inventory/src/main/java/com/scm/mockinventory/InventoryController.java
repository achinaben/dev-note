package com.scm.mockinventory;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/inventory/v1")
public class InventoryController {
    private final Map<String, String> statusByOrder = new ConcurrentHashMap<>();

    @PostMapping("/reserve")
    public Map<String, Object> reserve(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody Map<String, Object> body) {
        String orderNo = String.valueOf(body.get("order_no"));
        statusByOrder.put(orderNo, "RESERVED");
        return Map.of(
                "reserve_id", "RSV-" + key,
                "order_no", orderNo,
                "status", "RESERVED"
        );
    }

    @PostMapping("/confirm")
    public Map<String, Object> confirm(@RequestBody Map<String, String> body) {
        String orderNo = body.get("order_no");
        statusByOrder.put(orderNo, "CONFIRMED");
        return Map.of("order_no", orderNo, "status", "CONFIRMED");
    }

    @PostMapping("/release")
    public Map<String, Object> release(@RequestBody Map<String, String> body) {
        String orderNo = body.get("order_no");
        statusByOrder.put(orderNo, "RELEASED");
        return Map.of("order_no", orderNo, "status", "RELEASED");
    }

    @PostMapping("/status")
    public Map<String, Object> status(@RequestBody Map<String, String> body) {
        String orderNo = body.get("order_no");
        return Map.of("order_no", orderNo, "status", statusByOrder.getOrDefault(orderNo, "NONE"));
    }
}
