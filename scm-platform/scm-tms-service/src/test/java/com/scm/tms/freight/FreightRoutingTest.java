package com.scm.tms.freight;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreightRoutingTest {

    @Test
    void pickCheapestCarrier() {
        Map<String, Object> sf = new LinkedHashMap<>();
        sf.put("carrier_code", "SF");
        sf.put("amount_minor", 1500);
        Map<String, Object> yto = new LinkedHashMap<>();
        yto.put("carrier_code", "YTO");
        yto.put("amount_minor", 1200);
        List<Map<String, Object>> options = List.of(sf, yto);
        var best = FreightApplicationService.pickCheapest(options);
        assertTrue(best.isPresent());
        assertEquals("YTO", best.get().get("carrier_code"));
    }
}
