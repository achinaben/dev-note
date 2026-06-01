package com.scm.tms.shipment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcShipmentStore implements ShipmentStore {
    private final JdbcTemplate jdbc;

    public JdbcShipmentStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ShipmentRecord> findByPackageNo(String packageNo) {
        return queryOne("package_no=?", packageNo);
    }

    @Override
    public Optional<ShipmentRecord> findByShipmentNo(String shipmentNo) {
        return queryOne("shipment_no=?", shipmentNo);
    }

    @Override
    public Optional<ShipmentRecord> findByWaybillNo(String waybillNo) {
        return queryOne("waybill_no=?", waybillNo);
    }

    private Optional<ShipmentRecord> queryOne(String where, String arg) {
        var list = jdbc.query(
                "SELECT shipment_no,package_no,order_no,waybill_no,carrier_code,label_url,status FROM tms_shipment WHERE "
                        + where,
                this::map, arg);
        return list.stream().findFirst();
    }

    @Override
    public ShipmentRecord insert(ShipmentRecord record) {
        jdbc.update("""
                INSERT INTO tms_shipment(shipment_no,package_no,order_no,waybill_no,carrier_code,label_url,status)
                VALUES(?,?,?,?,?,?,?)
                """,
                record.getShipmentNo(), record.getPackageNo(), record.getOrderNo(),
                record.getWaybillNo(), record.getCarrierCode(), record.getLabelUrl(), record.getStatus());
        return record;
    }

    @Override
    public void updateStatusByPackageNo(String packageNo, String status) {
        jdbc.update("UPDATE tms_shipment SET status=? WHERE package_no=?", status, packageNo);
    }

    @Override
    public void updateStatusByShipmentNo(String shipmentNo, String status) {
        jdbc.update("UPDATE tms_shipment SET status=? WHERE shipment_no=?", status, shipmentNo);
    }

    @Override
    public void updateWaybillByPackageNo(String packageNo, String waybillNo) {
        jdbc.update("UPDATE tms_shipment SET waybill_no=? WHERE package_no=?", waybillNo, packageNo);
    }

    @Override
    public long countByPackageNo(String packageNo) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tms_shipment WHERE package_no=?", Long.class, packageNo);
        return c == null ? 0 : c;
    }

    private ShipmentRecord map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        ShipmentRecord r = new ShipmentRecord();
        r.setShipmentNo(rs.getString("shipment_no"));
        r.setPackageNo(rs.getString("package_no"));
        r.setOrderNo(rs.getString("order_no"));
        r.setWaybillNo(rs.getString("waybill_no"));
        r.setCarrierCode(rs.getString("carrier_code"));
        r.setLabelUrl(rs.getString("label_url"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
