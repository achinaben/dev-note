package com.scm.spring.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class IntegrationAccessInterceptor implements HandlerInterceptor {
    private final IntegrationSecurityProperties properties;

    public IntegrationAccessInterceptor(IntegrationSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!properties.isRequireApiKey()) {
            return true;
        }
        String expected = properties.getApiKey();
        if (expected == null || expected.isBlank()) {
            return true;
        }
        String provided = request.getHeader("X-Integration-Key");
        if (expected.equals(provided)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"AUTH_10001\",\"message\":\"integration api key required\"}");
        return false;
    }
}
