package com.scm.tms.shipment;

import java.util.Optional;

public interface ShipmentStore {
    Optional<ShipmentRecord> findByPackageNo(String packageNo);

    Optional<ShipmentRecord> findByShipmentNo(String shipmentNo);

    Optional<ShipmentRecord> findByWaybillNo(String waybillNo);

    ShipmentRecord insert(ShipmentRecord record);

    void updateStatusByPackageNo(String packageNo, String status);

    void updateStatusByShipmentNo(String shipmentNo, String status);

    void updateWaybillByPackageNo(String packageNo, String waybillNo);

    long countByPackageNo(String packageNo);
}
