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

/**
 * 契约来源：ai-dev openapi-oms-core POST /orders
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreateOrderContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createOrderRequestAndResponseMatchContract() throws Exception {
        String body;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/create-order.json")) {
            body = new String(in.readAllBytes());
        }
        JsonNode requestNode = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/create-order-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(requestNode);
        }

        String token = "ct-contract-" + System.nanoTime();
        JsonNode payload = requestNode.deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) payload).put("client_token", token);

        String response = mvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode responseNode = mapper.readTree(response);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/create-order-response.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(responseNode);
        }
    }
}
