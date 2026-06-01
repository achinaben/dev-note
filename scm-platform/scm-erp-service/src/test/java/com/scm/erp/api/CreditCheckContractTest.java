package com.scm.erp.api;

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
class CreditCheckContractTest {

    @Autowired
    MockMvc mvc;

    @Test
    void creditDeniedWhenOrderExceedsAvailable() throws Exception {
        String body = """
                {
                  "partner_id": "P-BIG",
                  "org_id": "ORG001",
                  "currency": "CNY",
                  "order_amount": "500000.0000"
                }
                """;
        mvc.perform(post("/api/v1/credit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.allowed").value(false));
    }

    @Test
    void creditAllowedWithinLimit() throws Exception {
        String body = """
                {
                  "partner_id": "C10001",
                  "org_id": "ORG001",
                  "currency": "CNY",
                  "order_amount": "1000.0000"
                }
                """;
        mvc.perform(post("/api/v1/credit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true));
    }
}
