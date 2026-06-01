package com.scm.spring.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationAccessInterceptorTest {

    @Test
    void skipsWhenApiKeyNotRequired() throws Exception {
        IntegrationSecurityProperties props = new IntegrationSecurityProperties();
        props.setRequireApiKey(false);
        var interceptor = new IntegrationAccessInterceptor(props);
        assertTrue(interceptor.preHandle(request(), response(), null));
    }

    @Test
    void rejectsMissingIntegrationKey() throws Exception {
        IntegrationSecurityProperties props = new IntegrationSecurityProperties();
        props.setRequireApiKey(true);
        props.setApiKey("secret");
        var interceptor = new IntegrationAccessInterceptor(props);
        MockHttpServletResponse resp = response();
        assertFalse(interceptor.preHandle(request(), resp, null));
        assertTrue(resp.getContentAsString().contains("AUTH_10001"));
        assertTrue(resp.getStatus() == 401);
    }

    @Test
    void acceptsMatchingKey() throws Exception {
        IntegrationSecurityProperties props = new IntegrationSecurityProperties();
        props.setRequireApiKey(true);
        props.setApiKey("secret");
        var interceptor = new IntegrationAccessInterceptor(props);
        MockHttpServletRequest req = request();
        req.addHeader("X-Integration-Key", "secret");
        assertTrue(interceptor.preHandle(req, response(), null));
    }

    private static MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/api/v1/integration/x");
    }

    private static MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }
}
