package com.scm.tms.shipment;

import com.scm.tms.track.InMemoryTrackEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreatePreservesHandoverWaybillTest {

    private ShipmentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ShipmentApplicationService(new InMemoryShipmentStore(), new InMemoryTrackEventRepository());
    }

    @Test
    void omsCreateDoesNotOverwriteHandoverWaybill() {
        String pkg = "P-oms-preserve-" + System.nanoTime();
        service.bindWaybillFromHandover(pkg, "O-1", "WB-KEEP-001");
        var replay = service.create(Map.of(
                "package_no", pkg,
                "order_no", "O-1",
                "carrier_code", "YTO",
                "waybill_no", "YTO-FIX-001"));
        assertEquals("WB-KEEP-001", replay.data().get("waybill_no"));
        assertEquals("WB-KEEP-001", service.findByWaybillNo("WB-KEEP-001").orElseThrow().getWaybillNo());
    }

    @Test
    void omsCreateFillsPlaceholderFromEvent() {
        String pkg = "P-oms-fill-" + System.nanoTime();
        service.create(Map.of("package_no", pkg, "order_no", "O-2", "carrier_code", "SF"));
        var replay = service.create(Map.of(
                "package_no", pkg,
                "order_no", "O-2",
                "carrier_code", "SF",
                "waybill_no", "WB-FROM-EVENT"));
        assertEquals("WB-FROM-EVENT", replay.data().get("waybill_no"));
    }
}
