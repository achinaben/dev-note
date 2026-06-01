package com.scm.wms.pick;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** operation_id 幂等存储；完整拣货流程见 RfPickConfirmContractTest */
class PickConfirmIdempotentTest {

    @Test
    void operationIdRecordedOnce() {
        MemoryPickConfirmStore store = new MemoryPickConfirmStore();
        assertFalse(store.exists("op-1"));
        store.save("op-1", "OB-test-1");
        assertTrue(store.exists("op-1"));
        assertEquals("OB-test-1", store.findOutboundNo("op-1").orElseThrow());
        store.save("op-1", "OB-test-1");
        assertEquals(1, store.findOutboundNo("op-1").stream().count());
    }
}
