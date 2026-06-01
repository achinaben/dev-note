package com.scm.oms.api;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TmsTrackContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void trackRequestMatchesContract() throws Exception {
        String token = "ct-track-" + System.nanoTime();
        String createBody = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(token);
        String orderNo = mapper.readTree(mvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString())
                .path("data").path("orders").get(0).path("order_no").asText();

        String trackBody = """
                {"order_no":"%s","event":"TMS_IN_TRANSIT"}
                """.formatted(orderNo);
        JsonNode node = mapper.readTree(trackBody);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/tms-track-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }
        mvc.perform(post("/api/v1/integration/tms/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackBody))
                .andExpect(status().isOk());
    }
}
