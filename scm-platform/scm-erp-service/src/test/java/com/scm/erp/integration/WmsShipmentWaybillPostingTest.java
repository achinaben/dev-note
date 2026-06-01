package com.scm.erp.integration;

import com.scm.erp.fi.JournalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WmsShipmentWaybillPostingTest {

    @Autowired
    WmsShipmentPostingService postingService;

    @Autowired
    JournalRepository journalRepository;

    @Test
    void postingStoresWaybillOnJournal() {
        String bizKey = "WMS_OUTBOUND_SHIPPED+OB-wb-" + System.nanoTime();
        postingService.postShipment(bizKey, new WmsShipmentPostingService.WmsShipmentRequest(
                "OB-wb-001",
                "O-wb-001",
                "ORG001",
                "WH-SH-01",
                List.of(Map.of("material_code", "M001", "qty", "2")),
                "WB-ERP-001"));
        var je = journalRepository.findByBizKey(bizKey).orElseThrow();
        assertEquals("WB-ERP-001", je.getWaybillNo());
    }
}
