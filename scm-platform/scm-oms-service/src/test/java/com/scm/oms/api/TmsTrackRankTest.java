package com.scm.oms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.scm.oms.integration.IntegrationStubConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E-08：轨迹乱序时状态 rank 不回退。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationStubConfiguration.class)
class TmsTrackRankTest {

    @Autowired
    MockMvc mvc;

    @Test
    void deliveredThenInTransitStaysDelivered() throws Exception {
        String token = "ct-rank-" + System.nanoTime();
        String createBody = """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(token);

        ObjectMapper mapper = new ObjectMapper();
        String createResp = mvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = mapper.readTree(createResp);
        String orderNo = root.path("data").path("orders").get(0).path("order_no").asText();

        String payBody = """
                {"notify_id":"n-%s","order_no":"%s","out_trade_no":"PAY-%s",
                 "amount_minor":19900,"sign_verified":true}
                """.formatted(token, orderNo, orderNo);
        mvc.perform(post("/api/v1/payments/notify/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payBody))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/ops/orders/{no}/ship", orderNo))
                .andExpect(status().isOk());

        String track = """
                {"order_no":"%s","event":"%s"}
                """;

        mvc.perform(post("/api/v1/integration/tms/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(track.formatted(orderNo, "TMS_DELIVERED")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/integration/tms/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(track.formatted(orderNo, "TMS_IN_TRANSIT")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/orders/{no}", orderNo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
    }
}
