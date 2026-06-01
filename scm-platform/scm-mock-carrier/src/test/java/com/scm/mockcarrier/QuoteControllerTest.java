package com.scm.mockcarrier;

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
class QuoteControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void quoteReturnsTwoCarriers() throws Exception {
        mvc.perform(post("/carrier/v1/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from_warehouse_code\":\"WH-SH-01\",\"receiver\":{},\"packages\":[{}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[0].carrier_code").exists())
                .andExpect(jsonPath("$.options[1].carrier_code").exists());
    }
}
