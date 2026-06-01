package com.scm.erp.inventory;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryLedgerRepository {
    private final Map<String, InventoryLedgerRecord> store = new ConcurrentHashMap<>();

    private static String key(String orgId, String whCode, String materialCode) {
        return orgId + "::" + whCode + "::" + materialCode;
    }

    public InventoryLedgerRecord getOrCreate(String orgId, String whCode, String materialCode) {
        return store.computeIfAbsent(key(orgId, whCode, materialCode), k -> {
            InventoryLedgerRecord r = new InventoryLedgerRecord();
            r.setOrgId(orgId);
            r.setWhCode(whCode);
            r.setMaterialCode(materialCode);
            return r;
        });
    }
}
