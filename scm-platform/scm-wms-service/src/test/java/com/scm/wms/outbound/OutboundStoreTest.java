package com.scm.wms.outbound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboundStoreTest {
    @Test
    void countOnePerPackage() {
        OutboundStore store = new InMemoryOutboundStore();
        OutboundRecord r = new OutboundRecord();
        r.setOutboundNo("OB-test-001");
        r.setPackageNo("P-test-001");
        r.setSourceOrderNo("O-test-001");
        r.setStatus("CREATED");
        store.insert(r);
        assertEquals(1, store.countByPackageNo("P-test-001"));
        assertEquals(0, store.countByPackageNo("P-other"));
    }
}
