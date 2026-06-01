package com.scm.tms.shipment;

import com.scm.tms.track.InMemoryTrackEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BindWaybillFromHandoverTest {

    private ShipmentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ShipmentApplicationService(new InMemoryShipmentStore(), new InMemoryTrackEventRepository());
    }

    @Test
    void bindCreatesShipmentWithHandoverWaybill() {
        String pkg = "P-bind-" + System.nanoTime();
        service.bindWaybillFromHandover(pkg, "O-001", "WB-HAND-001");
        var r = service.findByWaybillNo("WB-HAND-001").orElseThrow();
        assertEquals(pkg, r.getPackageNo());
        assertEquals("WB-HAND-001", r.getWaybillNo());
        assertFalse(service.listTrackEvents("WB-HAND-001").isEmpty());
    }

    @Test
    void bindUpdatesExistingWaybill() {
        String pkg = "P-upd-" + System.nanoTime();
        service.create(java.util.Map.of("package_no", pkg, "order_no", "O-002", "carrier_code", "SF"));
        service.bindWaybillFromHandover(pkg, "O-002", "WB-UPD-99");
        var r = service.findByWaybillNo("WB-UPD-99").orElseThrow();
        assertEquals("WB-UPD-99", r.getWaybillNo());
    }
}
