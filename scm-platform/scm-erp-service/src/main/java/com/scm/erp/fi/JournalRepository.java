package com.scm.erp.fi;

import java.util.Optional;

public interface JournalRepository {
    Optional<JournalEntryRecord> findByBizKey(String bizKey);

    JournalEntryRecord save(JournalEntryRecord entry);

    long count();
}
