package com.scm.oms.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "scm.security.jwt.enabled=true",
        "scm.security.jwt.issuer=http://localhost:8180/realms/scm",
        "scm.security.jwt.required-scope=oms.write"
})
class JwtScopeAccessTest {

    @Autowired
    MockMvc mvc;

    @Test
    void rejectsMissingToken() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .header("Idempotency-Key", "ct-jwt-miss")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody("ct-jwt-miss")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtCheckEndpointForAuthRequest() throws Exception {
        mvc.perform(get("/internal/v1/jwt/check"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/internal/v1/jwt/check")
                        .header("Authorization", bearerWithScope("oms.write", "http://localhost:8180/realms/scm")))
                .andExpect(status().isOk());
    }

    @Test
    void acceptsTokenWithScope() throws Exception {
        String token = bearerWithScope("oms.write", "http://localhost:8180/realms/scm");
        mvc.perform(post("/api/v1/orders")
                        .header("Authorization", token)
                        .header("Idempotency-Key", "ct-jwt-ok")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody("ct-jwt-ok")))
                .andExpect(status().isCreated());
    }

    private static String orderBody(String clientToken) {
        return """
                {"client_token":"%s","buyer_id":"U10001","channel":"APP","address_id":"ADDR100",
                 "lines":[{"sku_id":"SKU001","qty":"2","warehouse_id":"WH-SH-01"}]}
                """.formatted(clientToken);
    }

    static String bearerWithScope(String scope, String issuer) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payloadJson = """
                {"iss":"%s","scope":"%s","exp":9999999999}
                """.formatted(issuer, scope);
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "Bearer " + header + "." + payload + ".sig";
    }
}
