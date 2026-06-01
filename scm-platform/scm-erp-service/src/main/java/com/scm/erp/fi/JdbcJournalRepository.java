package com.scm.erp.fi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcJournalRepository implements JournalRepository {
    private final JdbcTemplate jdbc;
    private final AtomicLong seq = new AtomicLong(1);

    public JdbcJournalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<JournalEntryRecord> findByBizKey(String bizKey) {
        var list = jdbc.query(
                "SELECT je_no,biz_key,status,waybill_no FROM journal_entry WHERE biz_key=?",
                (rs, i) -> {
                    JournalEntryRecord e = new JournalEntryRecord();
                    e.setJeNo(rs.getString("je_no"));
                    e.setBizKey(rs.getString("biz_key"));
                    e.setStatus(rs.getString("status"));
                    e.setWaybillNo(rs.getString("waybill_no"));
                    return e;
                }, bizKey);
        return list.stream().findFirst();
    }

    @Override
    public JournalEntryRecord save(JournalEntryRecord entry) {
        if (entry.getJeNo() == null) {
            entry.setJeNo("JE" + String.format("%010d", seq.getAndIncrement()));
        }
        jdbc.update(
                "INSERT INTO journal_entry(je_no,biz_key,status,waybill_no) VALUES(?,?,?,?)",
                entry.getJeNo(), entry.getBizKey(), entry.getStatus(), entry.getWaybillNo());
        return entry;
    }

    @Override
    public long count() {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM journal_entry", Long.class);
        return c == null ? 0 : c;
    }
}
