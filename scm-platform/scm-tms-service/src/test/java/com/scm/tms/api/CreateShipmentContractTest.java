package com.scm.tms.api;

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

/**
 * 契约来源：ai-dev openapi-tms-core POST /shipment/create
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreateShipmentContractTest {

    @Autowired
    MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createShipmentRequestMatchesContract() throws Exception {
        String body = """
                {"package_no":"P-contract","order_no":"O-contract","weight_kg":"1.2","volume_cm3":8000,
                 "sender":{"name":"上海仓"},
                 "receiver":{"name":"客户","phone":"13800000000","province":"浙江省","city":"杭州市",
                             "district":"余杭区","address":"文一西路"}}
                """;
        JsonNode requestNode = mapper.readTree(body);
        try (InputStream schema = getClass().getResourceAsStream("/contracts/create-shipment-request.schema.json")) {
            new SchemaValidator(schema).validateOrThrow(requestNode);
        }
        mvc.perform(post("/tms/v1/shipment/create").header("Idempotency-Key", "P-contract")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }
}
