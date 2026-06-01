package com.scm.tms.api;

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
class ShipmentIdempotentTest {
    @Autowired
    MockMvc mvc;

    @Test
    void duplicatePackageConflict() throws Exception {
        String body = """
                {"package_no":"P-tms-dup","order_no":"O-tms-dup","weight_kg":"1.0",
                 "sender":{"name":"s"},"receiver":{"name":"r","phone":"1","province":"p","city":"c","district":"d","address":"a"}}
                """;
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-tms-dup")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-tms-dup")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TMS_10001"));
    }
}
