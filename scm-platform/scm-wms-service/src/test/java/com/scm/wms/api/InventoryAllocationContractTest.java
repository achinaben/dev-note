package com.scm.wms.api;

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

/**
 * 契约来源：ai-dev openapi-inventory POST /reserve、/confirm
 */
@SpringBootTest
@AutoConfigureMockMvc
class InventoryAllocationContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void reserveAndConfirmMatchInventoryContract() throws Exception {
        String orderNo = "O-inv-contract-" + System.nanoTime();
        String reserveBody = """
                {"client_token":"ct-%s","order_no":"%s","warehouse_id":"WH-SH-01",
                 "lines":[{"sku_id":"SKU001","qty":"2"}]}
                """.formatted(orderNo, orderNo);
        JsonNode node = mapper.readTree(reserveBody);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/inventory-reserve-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }
        mvc.perform(post("/inventory/v1/reserve")
                        .header("Idempotency-Key", orderNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reserveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));

        String confirmBody = """
                {"order_no":"%s","reserve_id":"RSV-%s"}
                """.formatted(orderNo, orderNo);
        mvc.perform(post("/inventory/v1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mvc.perform(post("/inventory/v1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"order_no\":\"" + orderNo + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
