package com.scm.tms.freight;

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
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.scm.tms.support.WireMockTestServer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 模拟 mock-carrier 双承运商报价与择优（YTO 更便宜）。 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIf("com.scm.tms.support.WireMockTestServer#isSupportedJvm")
class FreightEstimateMockCarrierTest {

    private static WireMockServer carrierMock;

    @BeforeAll
    static void start() {
        carrierMock = WireMockTestServer.startDynamic();
        carrierMock.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/carrier/v1/quote"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"options":[
                                  {"carrier_code":"SF","amount_minor":1500,"eta_days":2},
                                  {"carrier_code":"YTO","amount_minor":1200,"eta_days":3}
                                ]}
                                """)));
    }

    @AfterAll
    static void stop() {
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

    @Test
    void estimateRecommendsCheapestCarrier() throws Exception {
        mvc.perform(post("/tms/v1/freight/estimate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"from_warehouse_code":"WH-SH-01","receiver":{"city":"杭州"},
                                 "packages":[{"weight_kg":"1"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommended.carrier_code").value("YTO"));
    }
}
