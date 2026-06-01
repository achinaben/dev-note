package com.scm.oms.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcPaymentNotifyStore implements PaymentNotifyStore {
    private final JdbcTemplate jdbc;

    public JdbcPaymentNotifyStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void recordSuccess(String orderNo, String notifyId) {
        try {
            jdbc.update(
                    "INSERT INTO order_payment(order_no,notify_id,status) VALUES(?,?,?)",
                    orderNo, notifyId, "SUCCESS");
        } catch (DuplicateKeyException ignored) {
            // idempotent notify
        }
    }

    @Override
    public int successCount(String orderNo) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM order_payment WHERE order_no=? AND status='SUCCESS'",
                Integer.class, orderNo);
        return c == null ? 0 : c;
    }
}
