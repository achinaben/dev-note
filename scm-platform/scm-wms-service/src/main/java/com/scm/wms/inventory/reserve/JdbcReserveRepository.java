package com.scm.wms.inventory.reserve;

import com.scm.wms.inventory.fefo.AllocationLine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcReserveRepository implements ReserveRepository {

    private final JdbcTemplate jdbc;

    public JdbcReserveRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<OrderReserve> findByIdempotencyKey(String key) {
        var headers = jdbc.query(
                "SELECT order_no, idempotency_key, status FROM inv_order_reserve WHERE idempotency_key = ?",
                (rs, i) -> new String[]{rs.getString("order_no"), rs.getString("idempotency_key"), rs.getString("status")},
                key);
        if (headers.isEmpty()) {
            return Optional.empty();
        }
        String[] h = headers.get(0);
        return Optional.of(new OrderReserve(h[0], h[1], h[2], loadLines(h[0])));
    }

    @Override
    public Optional<OrderReserve> findByOrderNo(String orderNo) {
        var headers = jdbc.query(
                "SELECT order_no, idempotency_key, status FROM inv_order_reserve WHERE order_no = ?",
                (rs, i) -> new String[]{rs.getString("order_no"), rs.getString("idempotency_key"), rs.getString("status")},
                orderNo);
        if (headers.isEmpty()) {
            return Optional.empty();
        }
        String[] h = headers.get(0);
        return Optional.of(new OrderReserve(h[0], h[1], h[2], loadLines(h[0])));
    }

    @Override
    public void save(OrderReserve reserve) {
        try {
            jdbc.update(
                    "INSERT INTO inv_order_reserve(order_no, idempotency_key, status) VALUES(?,?,?)",
                    reserve.orderNo(), reserve.idempotencyKey(), reserve.status());
            for (AllocationLine line : reserve.lines()) {
                jdbc.update(
                        "INSERT INTO inv_reserve_line(order_no, lot_id, qty) VALUES(?,?,?)",
                        reserve.orderNo(), line.lotId(), line.qty());
            }
        } catch (DuplicateKeyException e) {
            throw e;
        }
    }

    @Override
    public void updateStatus(String orderNo, String status) {
        jdbc.update("UPDATE inv_order_reserve SET status = ? WHERE order_no = ?", status, orderNo);
    }

    private List<AllocationLine> loadLines(String orderNo) {
        return jdbc.query(
                "SELECT lot_id, qty FROM inv_reserve_line WHERE order_no = ?",
                (rs, i) -> new AllocationLine(rs.getString("lot_id"), rs.getInt("qty")),
                orderNo);
    }
}
