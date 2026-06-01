package com.scm.spring.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAccessInterceptorTest {

    @Test
    void excludesOpsPath() throws Exception {
        JwtSecurityProperties props = jwtProps();
        var interceptor = new JwtAccessInterceptor(props);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/ops/orders/O1/diag");
        assertTrue(interceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    @Test
    void rejectsMissingBearer() throws Exception {
        JwtSecurityProperties props = jwtProps();
        var interceptor = new JwtAccessInterceptor(props);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(orderRequest(null), resp, null));
        assertTrue(resp.getStatus() == 401);
    }

    @Test
    void acceptsValidScope() throws Exception {
        JwtSecurityProperties props = jwtProps();
        var interceptor = new JwtAccessInterceptor(props);
        MockHttpServletRequest req = orderRequest(unsignedBearer("oms.write"));
        assertTrue(interceptor.preHandle(req, new MockHttpServletResponse(), null));
    }

    private static JwtSecurityProperties jwtProps() {
        JwtSecurityProperties props = new JwtSecurityProperties();
        props.setEnabled(true);
        props.setVerifySignature(false);
        props.setIssuer("http://localhost:8180/realms/scm");
        props.setRequiredScope("oms.write");
        return props;
    }

    private static MockHttpServletRequest orderRequest(String authorization) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/orders");
        if (authorization != null) {
            req.addHeader("Authorization", authorization);
        }
        return req;
    }

    private static String unsignedBearer(String scope) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"iss\":\"http://localhost:8180/realms/scm\",\"scope\":\""
                        + scope + "\",\"exp\":9999999999}").getBytes(StandardCharsets.UTF_8));
        return "Bearer " + header + "." + payload + ".sig";
    }
}
