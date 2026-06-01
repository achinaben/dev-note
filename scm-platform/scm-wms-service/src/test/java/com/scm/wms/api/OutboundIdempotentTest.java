package com.scm.wms.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OutboundIdempotentTest {
    @Autowired
    MockMvc mvc;

    @Test
    void duplicatePackageConflict() throws Exception {
        String body = """
                {"package_no":"P-fix-001","source_order_no":"O-fix-001","warehouse_code":"WH-SH-01",
                 "delivery_type":"EXPRESS","lines":[{"sku_code":"SKU001","qty":"2"}],
                 "receiver":{"name":"t","phone":"1","province":"z","city":"h","district":"y","address":"a"}}
                """;
        mvc.perform(post("/wms/v1/outbound/create").header("Idempotency-Key", "P-fix-001")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/wms/v1/outbound/create").header("Idempotency-Key", "P-fix-001")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }
}
