package com.scm.wms.outbound;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcOutboundStore implements OutboundStore {
    private final JdbcTemplate jdbc;

    public JdbcOutboundStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<OutboundRecord> findByPackageNo(String packageNo) {
        var list = jdbc.query(
                "SELECT outbound_no,package_no,source_order_no,status FROM outbound_order WHERE package_no=?",
                this::map, packageNo);
        return list.stream().findFirst();
    }

    @Override
    public Optional<OutboundRecord> findByOutboundNo(String outboundNo) {
        var list = jdbc.query(
                "SELECT outbound_no,package_no,source_order_no,status FROM outbound_order WHERE outbound_no=?",
                this::map, outboundNo);
        return list.stream().findFirst();
    }

    @Override
    public Optional<String> findOutboundNoBySourceOrder(String sourceOrderNo) {
        var list = jdbc.query(
                "SELECT outbound_no FROM outbound_order WHERE source_order_no=? ORDER BY created_at LIMIT 1",
                (rs, i) -> rs.getString("outbound_no"), sourceOrderNo);
        return list.stream().findFirst();
    }

    @Override
    public OutboundRecord insert(OutboundRecord record) {
        try {
            jdbc.update(
                    "INSERT INTO outbound_order(outbound_no,package_no,source_order_no,status) VALUES(?,?,?,?)",
                    record.getOutboundNo(), record.getPackageNo(), record.getSourceOrderNo(), record.getStatus());
            return record;
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }

    @Override
    public void updateStatus(String outboundNo, String status) {
        jdbc.update("UPDATE outbound_order SET status=? WHERE outbound_no=?", status, outboundNo);
    }

    @Override
    public long countByPackageNo(String packageNo) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM outbound_order WHERE package_no=?", Long.class, packageNo);
        return c == null ? 0 : c;
    }

    private OutboundRecord map(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        OutboundRecord r = new OutboundRecord();
        r.setOutboundNo(rs.getString("outbound_no"));
        r.setPackageNo(rs.getString("package_no"));
        r.setSourceOrderNo(rs.getString("source_order_no"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
