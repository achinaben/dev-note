package com.scm.tms.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreateShipmentCarrierTest {

    @Autowired
    MockMvc mvc;

    @Test
    void createShipmentHonorsCarrierCode() throws Exception {
        String body = """
                {"package_no":"P-yto-1","order_no":"O-yto-1","carrier_code":"YTO","weight_kg":"1.0",
                 "sender":{"name":"s"},"receiver":{"name":"r","phone":"1","province":"p","city":"c","district":"d","address":"a"}}
                """;
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-yto-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.carrier_code").value("YTO"))
                .andExpect(jsonPath("$.data.waybill_no").value("YTO-FIX-001"));

        mvc.perform(get("/tms/v1/ops/shipment/count").queryParam("package_no", "P-yto-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }
}
