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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RfPickConfirmContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void pickConfirmIdempotentByOperationId() throws Exception {
        String suffix = "pick-" + System.nanoTime();
        String pkg = "P" + suffix;
        String ob = "OB" + suffix;
        String body = """
                {"task_no":"%s","operation_id":"op-pick-1","outbound_no":"%s",
                 "lines":[{"sku_code":"SKU001","qty":"2"}]}
                """.formatted(ob, ob);
        JsonNode node = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/rf-pick-confirm-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(node);
        }
        mvc.perform(post("/wms/v1/outbound/create").header("Idempotency-Key", pkg)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"package_no":"%s","source_order_no":"O-pick","warehouse_code":"WH-SH-01",
                                 "delivery_type":"EXPRESS","lines":[{"sku_code":"SKU001","qty":"2"}],
                                 "receiver":{"name":"t","phone":"1","province":"z","city":"h","district":"y","address":"a"}}
                                """.formatted(pkg)))
                .andExpect(status().isCreated());

        mvc.perform(post("/rf/v1/pick/confirm").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED"))
                .andExpect(jsonPath("$.idempotent").value(false));

        mvc.perform(post("/rf/v1/pick/confirm").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idempotent").value(true));
    }
}
