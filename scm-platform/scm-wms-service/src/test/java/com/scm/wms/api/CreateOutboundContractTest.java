package com.scm.wms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.common.schema.SchemaValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreateOutboundContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createOutboundRequestMatchesContract() throws Exception {
        String pkg = "P-contract-" + System.nanoTime();
        String body = """
                {"package_no":"%s","source_order_no":"O-contract-001","warehouse_code":"WH-SH-01",
                 "delivery_type":"EXPRESS","lines":[{"sku_code":"SKU001","qty":"2"}],
                 "receiver":{"name":"t","phone":"1","province":"z","city":"h","district":"y","address":"a"}}
                """.formatted(pkg);
        JsonNode node = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/create-outbound-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }
        mvc.perform(post("/wms/v1/outbound/create")
                        .header("Idempotency-Key", pkg)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
