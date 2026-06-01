package com.scm.wms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "scm.integration.erp-shipment=off")
@AutoConfigureMockMvc
class OutboundShipIT {

    @Autowired
    MockMvc mvc;

    @Test
    void shipMarksOutboundShipped() throws Exception {
        String createBody = """
                {"package_no":"P-ship-001","source_order_no":"O-ship-001","warehouse_code":"WH-SH-01",
                 "delivery_type":"EXPRESS","lines":[{"sku_code":"SKU001","qty":"2"}],
                 "receiver":{"name":"t","phone":"1","province":"z","city":"h","district":"y","address":"a"}}
                """;
        mvc.perform(post("/wms/v1/outbound/create").header("Idempotency-Key", "P-ship-001")
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated());
        mvc.perform(post("/wms/v1/outbound/OB-ship-001/ship")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lines\":[{\"sku_code\":\"SKU001\",\"qty\":\"2.0000\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }
}
