package com.scm.spring.security.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Nginx {@code auth_request} 子请求校验 JWT（见 deploy/nginx-gateway-jwt.docker.conf）。
 */
@RestController
@ConditionalOnProperty(prefix = "scm.security.jwt", name = "enabled", havingValue = "true")
public class JwtCheckController {

    private final JwtClaimsValidator validator;

    public JwtCheckController(JwtSecurityProperties properties) {
        this.validator = new JwtClaimsValidator(properties);
    }

    @GetMapping("/internal/v1/jwt/check")
    public ResponseEntity<Void> check(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            validator.validate(authorization);
            return ResponseEntity.ok().build();
        } catch (JwtValidationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
