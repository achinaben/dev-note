package com.scm.oms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.oms.integration.IntegrationStubConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationStubConfiguration.class)
class PackageInitBeforePayTest {

    @Autowired
    MockMvc mvc;

    @Test
    void initTwoPackagesBeforePaymentKeepsOrderPayable() throws Exception {
        String token = "ct-pkg-before-pay-" + System.nanoTime();
        String createBody = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(token);

        String createResp = mvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = new ObjectMapper().readTree(createResp);
        String orderNo = root.path("data").path("orders").get(0).path("order_no").asText();

        mvc.perform(post("/api/v1/ops/orders/{orderNo}/packages/init-two", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(2));
        mvc.perform(get("/api/v1/orders/{orderNo}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CREATED"));

        String payBody = """
                {"notify_id":"n-%s","order_no":"%s","out_trade_no":"PAY-%s",
                 "amount_minor":19900,"sign_verified":true}
                """.formatted(token, orderNo, orderNo);
        mvc.perform(post("/api/v1/payments/notify/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payBody))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/orders/{orderNo}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }
}
