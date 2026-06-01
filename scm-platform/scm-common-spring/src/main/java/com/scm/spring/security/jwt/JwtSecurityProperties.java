package com.scm.spring.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "scm.security.jwt")
public class JwtSecurityProperties {

    private boolean enabled;
    private boolean verifySignature;
    private String issuer = "";
    private String jwksUri = "";
    private String requiredScope = "oms.write";
    private List<String> excludePathPrefixes = new ArrayList<>(List.of(
            "/api/v1/payments/notify/",
            "/api/v1/ops/",
            "/api/v1/integration/"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVerifySignature() {
        return verifySignature;
    }

    public void setVerifySignature(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public String getRequiredScope() {
        return requiredScope;
    }

    public void setRequiredScope(String requiredScope) {
        this.requiredScope = requiredScope;
    }

    public List<String> getExcludePathPrefixes() {
        return excludePathPrefixes;
    }

    public void setExcludePathPrefixes(List<String> excludePathPrefixes) {
        this.excludePathPrefixes = excludePathPrefixes;
    }
}
