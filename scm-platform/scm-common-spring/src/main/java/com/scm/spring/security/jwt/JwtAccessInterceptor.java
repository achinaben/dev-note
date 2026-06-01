package com.scm.spring.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtAccessInterceptor implements HandlerInterceptor {

    private final JwtSecurityProperties properties;
    private final JwtClaimsValidator validator;

    public JwtAccessInterceptor(JwtSecurityProperties properties) {
        this.properties = properties;
        this.validator = new JwtClaimsValidator(properties);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        for (String prefix : properties.getExcludePathPrefixes()) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        try {
            validator.validate(request.getHeader("Authorization"));
            return true;
        } catch (JwtValidationException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":\"JWT_AUTH_10001\",\"message\":\"" + escape(ex.getMessage()) + "\"}");
            return false;
        }
    }

    private static String escape(String msg) {
        return msg == null ? "" : msg.replace("\"", "'");
    }
}
