package com.scm.tms.shipment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShipmentStoreTest {
    @Test
    void oneShipmentPerPackage() {
        ShipmentStore store = new InMemoryShipmentStore();
        ShipmentRecord r = new ShipmentRecord();
        r.setShipmentNo("SH-P1");
        r.setPackageNo("P1");
        r.setWaybillNo("SF-FIX-001");
        r.setCarrierCode("SF");
        r.setStatus("CREATED");
        store.insert(r);
        assertEquals(1, store.countByPackageNo("P1"));
    }
}
