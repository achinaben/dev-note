package com.scm.tms.api;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.scm.tms.support.WireMockTestServer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("com.scm.tms.support.WireMockTestServer#isSupportedJvm")
class CarrierCallbackWireMockTest {

    private static WireMockServer omsMock;

    @BeforeAll
    static void startOmsMock() {
        omsMock = WireMockTestServer.startDynamic();
    }

    @AfterAll
    static void stopOmsMock() {
        if (omsMock != null) {
            omsMock.stop();
        }
    }

    @DynamicPropertySource
    static void omsUrl(DynamicPropertyRegistry registry) {
        registry.add("oms.base-url", () -> omsMock.baseUrl());
    }

    @Autowired
    MockMvc mvc;

    @Test
    void carrierCallbackForwardsTrackToOms() throws Exception {
        omsMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/api/v1/integration/tms/track"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":\"0\",\"data\":{\"status\":\"SHIPPED\"}}")));

        String create = """
                {"package_no":"P-wm-1","order_no":"O-wm-1","weight_kg":"1.0",
                 "sender":{"name":"s"},"receiver":{"name":"r","phone":"1","province":"p","city":"c","district":"d","address":"a"}}
                """;
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-wm-1")
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isCreated());

        String callback = """
                {"waybill_no":"SF-FIX-001","carrier_status":"DELIVERED",
                 "event_time":"2026-05-31T12:00:00+08:00"}
                """;
        mvc.perform(post("/tms/v1/integration/carrier/SF/callback")
                        .contentType(MediaType.APPLICATION_JSON).content(callback))
                .andExpect(status().isOk());

        omsMock.verify(postRequestedFor(urlEqualTo("/api/v1/integration/tms/track")));
    }
}
