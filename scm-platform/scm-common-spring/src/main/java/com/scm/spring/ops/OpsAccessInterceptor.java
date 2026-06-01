package com.scm.spring.ops;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class OpsAccessInterceptor implements HandlerInterceptor {
    private final OpsProperties opsProperties;

    public OpsAccessInterceptor(OpsProperties opsProperties) {
        this.opsProperties = opsProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!opsProperties.isEnabled()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"OPS_DISABLED\",\"message\":\"ops endpoints disabled\"}");
            return false;
        }
        String expectedKey = opsProperties.getApiKey();
        if (expectedKey != null && !expectedKey.isBlank()) {
            String provided = request.getHeader("X-Ops-Token");
            if (!expectedKey.equals(provided)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"OPS_AUTH_10001\",\"message\":\"invalid ops token\"}");
                return false;
            }
        }
        return true;
    }
}
