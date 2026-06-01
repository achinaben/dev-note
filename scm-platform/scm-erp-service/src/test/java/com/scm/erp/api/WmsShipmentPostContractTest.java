package com.scm.erp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.common.schema.SchemaValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WmsShipmentPostContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void postWmsShipmentMatchesContract() throws Exception {
        String ob = "OB-CONTRACT-" + System.nanoTime();
        String body = """
                {
                  "outbound_no":"%s",
                  "source_system":"WMS",
                  "source_order_no":"O-contract-erp",
                  "org_id":"ORG001",
                  "wh_code":"WH-SH-01",
                  "shipped_at":"2026-05-31T15:00:00+08:00",
                  "lines":[{"material_code":"M001","qty":"2.0000"}]
                }
                """.formatted(ob);
        JsonNode node = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/wms-shipment-post.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }
        String bizKey = "WMS_OUTBOUND_SHIPPED+" + ob;
        mvc.perform(post("/api/v1/integration/wms/shipment")
                        .header("Idempotency-Key", bizKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.je_no").exists());
    }
}
