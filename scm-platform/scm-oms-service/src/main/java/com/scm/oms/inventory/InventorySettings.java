package com.scm.oms.inventory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.inventory")
public class InventorySettings {

    private InventoryProvider provider = InventoryProvider.MOCK;
    private boolean remoteEnabled = true;
    private String baseUrl = "";
    private String wmsBaseUrl = "http://localhost:8082";

    public boolean useRemote() {
        return provider != InventoryProvider.LOCAL && remoteEnabled;
    }

    public String resolveBaseUrl() {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl.endsWith("/inventory/v1")
                    ? baseUrl
                    : baseUrl.replaceAll("/$", "") + "/inventory/v1";
        }
        return switch (provider) {
            case WMS -> wmsBaseUrl.replaceAll("/$", "") + "/inventory/v1";
            case MOCK -> "http://localhost:8087/inventory/v1";
            case LOCAL -> "";
        };
    }

    public InventoryProvider getProvider() {
        return provider;
    }

    public void setProvider(InventoryProvider provider) {
        this.provider = provider;
    }

    public boolean isRemoteEnabled() {
        return remoteEnabled;
    }

    public void setRemoteEnabled(boolean remoteEnabled) {
        this.remoteEnabled = remoteEnabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getWmsBaseUrl() {
        return wmsBaseUrl;
    }

    public void setWmsBaseUrl(String wmsBaseUrl) {
        this.wmsBaseUrl = wmsBaseUrl;
    }
}
