package com.scm.erp.integration;

import com.scm.common.tenant.OrgIdContext;
import com.scm.erp.inventory.InventoryLedgerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OrgIdPostingTest {

    @Autowired
    WmsShipmentPostingService postingService;

    @Autowired
    InventoryLedgerRepository ledgerRepository;

    @Test
    void postingUsesOrgIdFromRequest() {
        OrgIdContext.set("ORG002");
        try {
            String bizKey = "WMS_OUTBOUND_SHIPPED+OB-ORG-TEST";
            var req = new WmsShipmentPostingService.WmsShipmentRequest(
                    "OB-ORG-TEST",
                    "O-org-test",
                    "ORG002",
                    "WH-SH-01",
                    List.of(Map.of("material_code", "M001", "qty", "2.0000"))
            );
            postingService.postShipment(bizKey, req);
            var ledger = ledgerRepository.getOrCreate("ORG002", "WH-SH-01", "M001");
            assertEquals("ORG002", ledger.getOrgId());
        } finally {
            OrgIdContext.clear();
        }
    }
}
