package com.scm.erp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WmsShipmentPostingIT {
    @Autowired
    MockMvc mvc;

    @Test
    void postAndIdempotent() throws Exception {
        String body = """
                {
                  "outbound_no":"OB20260531001",
                  "source_system":"WMS",
                  "source_order_no":"O-fix-001",
                  "org_id":"ORG001",
                  "wh_code":"WH-SH-01",
                  "shipped_at":"2026-05-31T15:00:00+08:00",
                  "lines":[{"material_code":"M001","qty":"2.0000"}]
                }
                """;
        String key = "WMS_OUTBOUND_SHIPPED+OB20260531001";
        mvc.perform(post("/api/v1/integration/wms/shipment")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.je_no").exists());
        mvc.perform(post("/api/v1/integration/wms/shipment")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ERP_02001"));
    }
}
