package com.scm.erp.integration;

import com.scm.common.idempotency.ProcessedMessageStore;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcProcessedMessageStore implements ProcessedMessageStore {
    private final JdbcTemplate jdbc;

    public JdbcProcessedMessageStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean exists(String bizKey, String consumerGroup) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM processed_message WHERE biz_key=? AND consumer_group=?",
                Integer.class, bizKey, consumerGroup);
        return c != null && c > 0;
    }

    @Override
    public void markProcessed(String bizKey, String consumerGroup, String eventId) {
        jdbc.update(
                "INSERT IGNORE INTO processed_message(biz_key,consumer_group,event_id) VALUES(?,?,?)",
                bizKey, consumerGroup, eventId);
    }
}
