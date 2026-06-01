package com.scm.wms.pick;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcPickConfirmStore implements PickConfirmStore {

    private final JdbcTemplate jdbc;

    public JdbcPickConfirmStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean exists(String operationId) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM wms_pick_confirm WHERE operation_id=?", Long.class, operationId);
        return c != null && c > 0;
    }

    @Override
    public void save(String operationId, String outboundNo) {
        try {
            jdbc.update(
                    "INSERT INTO wms_pick_confirm(operation_id,outbound_no) VALUES(?,?)",
                    operationId, outboundNo);
        } catch (DuplicateKeyException ignored) {
            // 并发幂等：已存在则忽略
        }
    }

    @Override
    public Optional<String> findOutboundNo(String operationId) {
        var list = jdbc.query(
                "SELECT outbound_no FROM wms_pick_confirm WHERE operation_id=?",
                (rs, i) -> rs.getString("outbound_no"), operationId);
        return list.stream().findFirst();
    }
}
