package com.scm.oms.order;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcOrderRepository implements OrderRepository {
    private final JdbcTemplate jdbc;
    private final AtomicLong seq = new AtomicLong(1);

    private static final RowMapper<OrderRecord> MAPPER = (rs, rowNum) -> map(rs);

    public JdbcOrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<OrderRecord> findByClientToken(String buyerId, String clientToken) {
        var list = jdbc.query(
                "SELECT * FROM trade_order WHERE buyer_id=? AND client_token=?",
                MAPPER, buyerId, clientToken);
        return list.stream().findFirst();
    }

    @Override
    public Optional<OrderRecord> findByOrderNo(String orderNo) {
        var list = jdbc.query("SELECT * FROM trade_order WHERE order_no=?", MAPPER, orderNo);
        return list.stream().findFirst();
    }

    @Override
    public OrderRecord saveNew(OrderRecord order) {
        if (order.getOrderNo() == null) {
            long n = seq.getAndIncrement();
            order.setOrderNo("O20260531" + String.format("%06d", n));
            order.setTradeNo("T20260531" + String.format("%06d", n));
        }
        order.setVersion(1);
        jdbc.update("""
                INSERT INTO trade_order(order_no,trade_no,buyer_id,client_token,status,pay_amount,pay_time,version)
                VALUES(?,?,?,?,?,?,?,?)
                """,
                order.getOrderNo(), order.getTradeNo(), order.getBuyerId(), order.getClientToken(),
                order.getStatus().name(), order.getPayAmount(), toTs(order.getPayTime()), order.getVersion());
        return order;
    }

    @Override
    public void update(OrderRecord order) {
        int rows = jdbc.update("""
                UPDATE trade_order SET status=?, pay_amount=?, pay_time=?, version=version+1
                WHERE order_no=? AND version=?
                """,
                order.getStatus().name(), order.getPayAmount(), toTs(order.getPayTime()),
                order.getOrderNo(), order.getVersion());
        if (rows == 1) {
            order.setVersion(order.getVersion() + 1);
        }
    }

    @Override
    public long countByBuyerAndToken(String buyerId, String clientToken) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM trade_order WHERE buyer_id=? AND client_token=?",
                Long.class, buyerId, clientToken);
        return c == null ? 0 : c;
    }

    private static OrderRecord map(ResultSet rs) throws SQLException {
        OrderRecord o = new OrderRecord();
        o.setOrderNo(rs.getString("order_no"));
        o.setTradeNo(rs.getString("trade_no"));
        o.setBuyerId(rs.getString("buyer_id"));
        o.setClientToken(rs.getString("client_token"));
        o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        o.setPayAmount(rs.getBigDecimal("pay_amount"));
        Timestamp pt = rs.getTimestamp("pay_time");
        if (pt != null) {
            o.setPayTime(pt.toInstant().atOffset(ZoneOffset.UTC));
        }
        o.setVersion(rs.getInt("version"));
        return o;
    }

    private static Timestamp toTs(OffsetDateTime t) {
        return t == null ? null : Timestamp.from(t.toInstant());
    }
}
