package com.scm.tms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.scm.common.schema.SchemaValidator;
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

import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.scm.tms.support.WireMockTestServer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("com.scm.tms.support.WireMockTestServer#isSupportedJvm")
class CarrierCallbackContractTest {

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

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void callbackRequestMatchesContract() throws Exception {
        omsMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/api/v1/integration/tms/track"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":\"0\"}")));

        String create = """
                {"package_no":"P-cb-1","order_no":"O-cb-1","weight_kg":"1.0",
                 "sender":{"name":"s"},"receiver":{"name":"r","phone":"1","province":"p","city":"c","district":"d","address":"a"}}
                """;
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-cb-1")
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isCreated());

        String callback = """
                {"waybill_no":"SF-FIX-001","carrier_status":"IN_TRANSIT",
                 "event_time":"2026-05-31T10:00:00+08:00","location":"杭州"}
                """;
        JsonNode node = mapper.readTree(callback);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/carrier-callback-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }

        mvc.perform(post("/tms/v1/integration/carrier/SF/callback")
                        .contentType(MediaType.APPLICATION_JSON).content(callback))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.forwarded_event").value("TMS_IN_TRANSIT"));
    }
}
