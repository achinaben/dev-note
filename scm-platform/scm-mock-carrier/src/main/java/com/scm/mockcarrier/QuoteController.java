package com.scm.mockcarrier;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/carrier/v1")
public class QuoteController {

    @PostMapping("/quote")
    public Map<String, Object> quote(@RequestBody Map<String, Object> body) {
        return Map.of(
                "options", List.of(
                        Map.of("carrier_code", "SF", "amount_minor", 1500, "eta_days", 2),
                        Map.of("carrier_code", "YTO", "amount_minor", 1200, "eta_days", 3)
                ),
                "from_warehouse_code", body.getOrDefault("from_warehouse_code", "WH-SH-01")
        );
    }
}
