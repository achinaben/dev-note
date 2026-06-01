package com.scm.oms.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcOutboxStore implements OutboxStore {
    private final JdbcTemplate jdbc;

    public JdbcOutboxStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void append(OutboxEvent event) {
        jdbc.update("""
                INSERT IGNORE INTO outbox_event(event_type,biz_key,occurred_at,payload_json,published)
                VALUES(?,?,?,?,0)
                """,
                event.eventType(), event.bizKey(), Timestamp.from(event.occurredAt().toInstant()),
                event.payloadJson());
    }

    @Override
    public boolean exists(String bizKey) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM outbox_event WHERE biz_key=?", Integer.class, bizKey);
        return c != null && c > 0;
    }

    @Override
    public boolean isPublished(String bizKey) {
        Integer c = jdbc.queryForObject(
                "SELECT published FROM outbox_event WHERE biz_key=?", Integer.class, bizKey);
        return c != null && c == 1;
    }

    @Override
    public List<OutboxEvent> pending() {
        return jdbc.query(
                "SELECT event_type,biz_key,occurred_at,payload_json FROM outbox_event WHERE published=0",
                (rs, i) -> new OutboxEvent(
                        rs.getString("event_type"),
                        rs.getString("biz_key"),
                        rs.getTimestamp("occurred_at").toInstant().atOffset(ZoneOffset.UTC),
                        rs.getString("payload_json")
                ));
    }

    @Override
    public void markPublished(String bizKey) {
        jdbc.update("UPDATE outbox_event SET published=1 WHERE biz_key=?", bizKey);
    }

    @Override
    public List<OutboxEvent> all() {
        return jdbc.query(
                "SELECT event_type,biz_key,occurred_at,payload_json FROM outbox_event",
                (rs, i) -> new OutboxEvent(
                        rs.getString("event_type"),
                        rs.getString("biz_key"),
                        rs.getTimestamp("occurred_at").toInstant().atOffset(ZoneOffset.UTC),
                        rs.getString("payload_json")
                ));
    }
}
