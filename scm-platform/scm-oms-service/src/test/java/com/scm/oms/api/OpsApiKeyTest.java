package com.scm.oms.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "scm.ops.enabled=true",
        "scm.ops.api-key=test-ops-secret"
})
class OpsApiKeyTest {

    @Autowired
    MockMvc mvc;

    @Test
    void opsRequiresTokenWhenConfigured() throws Exception {
        mvc.perform(get("/api/v1/ops/orders/O1/diag"))
                .andExpect(status().isUnauthorized());

        String token = "ct-ops-" + System.nanoTime();
        String createBody = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(token);
        String orderNo = mvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String no = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(orderNo).path("data").path("orders").get(0).path("order_no").asText();

        mvc.perform(get("/api/v1/ops/orders/{no}/diag", no)
                        .header("X-Ops-Token", "test-ops-secret"))
                .andExpect(status().isOk());
    }
}
