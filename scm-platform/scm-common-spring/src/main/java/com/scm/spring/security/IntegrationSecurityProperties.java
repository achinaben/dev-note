package com.scm.spring.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.integration")
public class IntegrationSecurityProperties {
    /** 生产 profile 建议 true，要求 X-Integration-Key */
    private boolean requireApiKey = false;
    private String apiKey = "";

    public boolean isRequireApiKey() {
        return requireApiKey;
    }

    public void setRequireApiKey(boolean requireApiKey) {
        this.requireApiKey = requireApiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
