package com.scm.oms.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "scm.ops.enabled=false")
class OpsDisabledTest {
    @Autowired
    MockMvc mvc;

    @Test
    void opsReturns404WhenDisabled() throws Exception {
        mvc.perform(get("/api/v1/ops/orders/O1/diag"))
                .andExpect(status().isNotFound());
    }
}
