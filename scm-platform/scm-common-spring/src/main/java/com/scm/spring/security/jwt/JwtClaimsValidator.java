package com.scm.spring.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

public class JwtClaimsValidator {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final JwtSecurityProperties properties;

    public JwtClaimsValidator(JwtSecurityProperties properties) {
        this.properties = properties;
    }

    public void validate(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new JwtValidationException("missing bearer token");
        }
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7).trim() : bearerToken.trim();
        JsonNode claims = properties.isVerifySignature()
                ? verifySignedAndParseClaims(token)
                : decodePayloadClaims(token);
        validateClaims(claims);
    }

    private JsonNode verifySignedAndParseClaims(String token) {
        if (properties.getJwksUri() == null || properties.getJwksUri().isBlank()) {
            throw new JwtValidationException("jwks-uri required when verify-signature=true");
        }
        try {
            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            var jwkSource = JWKSourceBuilder.create(URI.create(properties.getJwksUri()).toURL()).build();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
            JWTClaimsSet claimSet = processor.process(token, null);
            return JSON.readTree(claimSet.toJSONObject().toString());
        } catch (Exception e) {
            throw new JwtValidationException("jwt signature verification failed", e);
        }
    }

    private static JsonNode decodePayloadClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new JwtValidationException("invalid jwt format");
        }
        return decodePayload(parts[1]);
    }

    private void validateClaims(JsonNode claims) {
        if (properties.getIssuer() != null && !properties.getIssuer().isBlank()) {
            String iss = textOrEmpty(claims, "iss");
            if (!properties.getIssuer().equals(iss)) {
                throw new JwtValidationException("issuer mismatch");
            }
        }
        String required = properties.getRequiredScope();
        if (required != null && !required.isBlank() && !hasScope(claims, required)) {
            throw new JwtValidationException("required scope missing: " + required);
        }
        if (claims.has("exp") && claims.get("exp").asLong() < System.currentTimeMillis() / 1000) {
            throw new JwtValidationException("token expired");
        }
    }

    private static boolean hasScope(JsonNode claims, String required) {
        if (claims.has("scope")) {
            String scope = claims.get("scope").asText("");
            if (scope.contains(required)) {
                return true;
            }
        }
        JsonNode realmAccess = claims.get("realm_access");
        if (realmAccess != null && realmAccess.has("roles")) {
            for (JsonNode role : realmAccess.get("roles")) {
                if (required.equals(role.asText())) {
                    return true;
                }
            }
        }
        JsonNode resourceAccess = claims.get("resource_access");
        if (resourceAccess != null) {
            Iterator<String> clients = resourceAccess.fieldNames();
            while (clients.hasNext()) {
                JsonNode roles = resourceAccess.get(clients.next()).get("roles");
                if (roles != null) {
                    for (JsonNode role : roles) {
                        if (required.equals(role.asText())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static JsonNode decodePayload(String payloadPart) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(pad(payloadPart));
            return JSON.readTree(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new JwtValidationException("cannot decode jwt payload", e);
        }
    }

    private static String pad(String base64) {
        int mod = base64.length() % 4;
        if (mod == 0) {
            return base64;
        }
        return base64 + "====".substring(mod);
    }

    private static String textOrEmpty(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "";
    }
}
