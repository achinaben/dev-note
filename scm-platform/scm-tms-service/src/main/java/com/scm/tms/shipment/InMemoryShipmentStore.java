package com.scm.tms.shipment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryShipmentStore implements ShipmentStore {
    private final Map<String, ShipmentRecord> byPackage = new ConcurrentHashMap<>();

    @Override
    public Optional<ShipmentRecord> findByPackageNo(String packageNo) {
        return Optional.ofNullable(byPackage.get(packageNo));
    }

    @Override
    public Optional<ShipmentRecord> findByShipmentNo(String shipmentNo) {
        return byPackage.values().stream()
                .filter(r -> shipmentNo.equals(r.getShipmentNo()))
                .findFirst();
    }

    @Override
    public Optional<ShipmentRecord> findByWaybillNo(String waybillNo) {
        return byPackage.values().stream()
                .filter(r -> waybillNo.equals(r.getWaybillNo()))
                .findFirst();
    }

    @Override
    public ShipmentRecord insert(ShipmentRecord record) {
        byPackage.put(record.getPackageNo(), record);
        return record;
    }

    @Override
    public void updateStatusByPackageNo(String packageNo, String status) {
        ShipmentRecord r = byPackage.get(packageNo);
        if (r != null) {
            r.setStatus(status);
            byPackage.put(packageNo, r);
        }
    }

    @Override
    public void updateStatusByShipmentNo(String shipmentNo, String status) {
        findByShipmentNo(shipmentNo).ifPresent(r -> updateStatusByPackageNo(r.getPackageNo(), status));
    }

    @Override
    public void updateWaybillByPackageNo(String packageNo, String waybillNo) {
        ShipmentRecord r = byPackage.get(packageNo);
        if (r != null) {
            r.setWaybillNo(waybillNo);
            byPackage.put(packageNo, r);
        }
    }

    @Override
    public long countByPackageNo(String packageNo) {
        return byPackage.containsKey(packageNo) ? 1 : 0;
    }
}
