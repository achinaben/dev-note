package com.scm.oms.aftersale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcAfterSaleRepository implements AfterSaleRepository {
    private final JdbcTemplate jdbc;
    private final AtomicLong seq = new AtomicLong(1);

    public JdbcAfterSaleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AfterSaleRecord save(AfterSaleRecord record) {
        if (record.getAfterSaleNo() == null) {
            record.setAfterSaleNo("AS" + String.format("%08d", seq.getAndIncrement()));
        }
        jdbc.update(
                "INSERT INTO after_sale(after_sale_no,order_no,status,refund_amount) VALUES(?,?,?,?)",
                record.getAfterSaleNo(), record.getOrderNo(), record.getStatus(), record.getRefundAmount());
        return record;
    }

    @Override
    public Optional<AfterSaleRecord> findByOrderNo(String orderNo) {
        var list = jdbc.query(
                "SELECT * FROM after_sale WHERE order_no=? ORDER BY created_at DESC LIMIT 1",
                (rs, i) -> map(rs), orderNo);
        return list.stream().findFirst();
    }

    @Override
    public Optional<AfterSaleRecord> findByAfterSaleNo(String afterSaleNo) {
        var list = jdbc.query("SELECT * FROM after_sale WHERE after_sale_no=?", (rs, i) -> map(rs), afterSaleNo);
        return list.stream().findFirst();
    }

    @Override
    public void update(AfterSaleRecord record) {
        jdbc.update("UPDATE after_sale SET status=? WHERE after_sale_no=?",
                record.getStatus(), record.getAfterSaleNo());
    }

    private static AfterSaleRecord map(java.sql.ResultSet rs) throws java.sql.SQLException {
        AfterSaleRecord r = new AfterSaleRecord();
        r.setAfterSaleNo(rs.getString("after_sale_no"));
        r.setOrderNo(rs.getString("order_no"));
        r.setStatus(rs.getString("status"));
        r.setRefundAmount(rs.getBigDecimal("refund_amount"));
        return r;
    }
}
