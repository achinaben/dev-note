package com.scm.erp.fi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryJournalRepository implements JournalRepository {
    private final Map<String, JournalEntryRecord> byBizKey = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public Optional<JournalEntryRecord> findByBizKey(String bizKey) {
        return Optional.ofNullable(byBizKey.get(bizKey));
    }

    @Override
    public JournalEntryRecord save(JournalEntryRecord entry) {
        if (entry.getJeNo() == null) {
            entry.setJeNo("JE" + String.format("%010d", seq.getAndIncrement()));
        }
        byBizKey.put(entry.getBizKey(), entry);
        return entry;
    }

    @Override
    public long count() {
        return byBizKey.size();
    }
}
