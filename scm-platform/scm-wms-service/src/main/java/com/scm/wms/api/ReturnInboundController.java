package com.scm.wms.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/wms/v1/return")
public class ReturnInboundController {
    private final Map<String, String> returnStatus = new ConcurrentHashMap<>();

    @PostMapping("/inbound")
    public ResponseEntity<Map<String, String>> inbound(@RequestBody Map<String, String> body) {
        String orderNo = body.get("order_no");
        returnStatus.put(orderNo, "INBOUND_DONE");
        return ResponseEntity.ok(Map.of("order_no", orderNo, "status", "INBOUND_DONE"));
    }
}
