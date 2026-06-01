package com.scm.erp.integration;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FiscalPeriodService {
    private final Map<String, String> periodStatus = new ConcurrentHashMap<>();

    public void assertOpen(String orgId) {
        if ("CLOSED".equals(periodStatus.getOrDefault(orgId, "OPEN"))) {
            throw new IllegalStateException("ERP_01001");
        }
    }

    public void close(String orgId) {
        periodStatus.put(orgId, "CLOSED");
    }
}
