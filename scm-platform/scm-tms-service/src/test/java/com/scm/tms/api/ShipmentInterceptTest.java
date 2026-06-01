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
class ShipmentInterceptTest {

    @Autowired
    MockMvc mvc;

    @Test
    void interceptMarksShipmentIntercepted() throws Exception {
        String body = """
                {"package_no":"P-int-1","order_no":"O-int-1","weight_kg":"1.0",
                 "sender":{"name":"s"},"receiver":{"name":"r","phone":"1","province":"p","city":"c","district":"d","address":"a"}}
                """;
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-int-1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/tms/v1/shipment/{no}/intercept", "SHP-int-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INTERCEPTED"));
    }
}
