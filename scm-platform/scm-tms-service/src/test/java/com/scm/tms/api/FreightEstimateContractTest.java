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
class FreightEstimateContractTest {

    private static WireMockServer carrierMock;

    @BeforeAll
    static void startCarrierMock() {
        carrierMock = WireMockTestServer.startDynamic();
    }

    @AfterAll
    static void stopCarrierMock() {
        if (carrierMock != null) {
            carrierMock.stop();
        }
    }

    @DynamicPropertySource
    static void carrierUrl(DynamicPropertyRegistry registry) {
        registry.add("carrier.base-url", () -> carrierMock.baseUrl());
    }

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void estimateUsesWireMockCarrier() throws Exception {
        carrierMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/carrier/v1/quote"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"options":[{"carrier_code":"SF","amount_minor":1200,"eta_days":2}]}
                                """)));

        String body = """
                {"from_warehouse_code":"WH-SH-01","receiver":{"city":"杭州"},
                 "packages":[{"weight_kg":"1.0"}]}
                """;
        JsonNode node = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/freight-estimate-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }

        mvc.perform(post("/tms/v1/freight/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.options[0].carrier_code").value("SF"));
    }
}
