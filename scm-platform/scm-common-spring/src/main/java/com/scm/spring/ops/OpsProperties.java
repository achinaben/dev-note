package com.scm.spring.ops;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.ops")
public class OpsProperties {
    /** 联调 / E2E 辅助接口；生产应设为 false */
    private boolean enabled = true;
    /** 非空时访问 ops 需 Header X-Ops-Token */
    private String apiKey = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
