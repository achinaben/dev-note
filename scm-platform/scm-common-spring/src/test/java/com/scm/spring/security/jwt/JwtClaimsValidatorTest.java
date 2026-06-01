package com.scm.spring.security.jwt;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtClaimsValidatorTest {

    @Test
    void claimOnlyModeAcceptsUnsignedToken() {
        JwtSecurityProperties props = new JwtSecurityProperties();
        props.setEnabled(true);
        props.setVerifySignature(false);
        props.setIssuer("http://localhost:8180/realms/scm");
        props.setRequiredScope("oms.write");
        JwtClaimsValidator validator = new JwtClaimsValidator(props);
        assertDoesNotThrow(() -> validator.validate("Bearer " + unsignedToken()));
    }

    @Test
    void signatureModeRequiresJwksUri() {
        JwtSecurityProperties props = new JwtSecurityProperties();
        props.setVerifySignature(true);
        JwtClaimsValidator validator = new JwtClaimsValidator(props);
        assertThrows(JwtValidationException.class,
                () -> validator.validate("Bearer " + unsignedToken()));
    }

    @Test
    void rejectsMissingScope() {
        JwtSecurityProperties props = new JwtSecurityProperties();
        props.setVerifySignature(false);
        props.setIssuer("http://localhost:8180/realms/scm");
        props.setRequiredScope("oms.write");
        JwtClaimsValidator validator = new JwtClaimsValidator(props);
        assertThrows(JwtValidationException.class,
                () -> validator.validate("Bearer " + tokenWithPayload("""
                        {"iss":"http://localhost:8180/realms/scm","scope":"oms.read","exp":9999999999}
                        """)));
    }

    @Test
    void acceptsClientRoleScope() {
        JwtSecurityProperties props = new JwtSecurityProperties();
        props.setVerifySignature(false);
        props.setIssuer("http://localhost:8180/realms/scm");
        props.setRequiredScope("oms.write");
        JwtClaimsValidator validator = new JwtClaimsValidator(props);
        assertDoesNotThrow(() -> validator.validate("Bearer " + tokenWithPayload("""
                {"iss":"http://localhost:8180/realms/scm","exp":9999999999,
                 "resource_access":{"scm-gateway":{"roles":["oms.write"]}}}
                """)));
    }

    private static String tokenWithPayload(String json) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.trim().getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".sig";
    }

    private static String unsignedToken() {
        return tokenWithPayload("""
                {"iss":"http://localhost:8180/realms/scm","scope":"oms.write","exp":9999999999}
                """);
    }
}
