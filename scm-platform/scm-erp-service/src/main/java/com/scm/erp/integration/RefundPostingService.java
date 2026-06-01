package com.scm.erp.integration;

import com.scm.common.idempotency.ProcessedMessageStore;
import com.scm.erp.fi.JournalEntryRecord;
import com.scm.erp.fi.JournalRepository;
import org.springframework.stereotype.Service;

@Service
public class RefundPostingService {
    private static final String CONSUMER_GROUP = "erp-refund-cg";

    private final ProcessedMessageStore processedMessageStore;
    private final JournalRepository journalRepository;

    public RefundPostingService(
            ProcessedMessageStore processedMessageStore,
            JournalRepository journalRepository) {
        this.processedMessageStore = processedMessageStore;
        this.journalRepository = journalRepository;
    }

    public PostingResult postRefund(String bizKey, String orderNo, String amount) {
        if (processedMessageStore.exists(bizKey, CONSUMER_GROUP)) {
            JournalEntryRecord existing = journalRepository.findByBizKey(bizKey).orElseThrow();
            return PostingResult.idempotent(existing.getJeNo());
        }
        JournalEntryRecord je = new JournalEntryRecord();
        je.setBizKey(bizKey);
        je.setStatus("POSTED");
        journalRepository.save(je);
        processedMessageStore.markProcessed(bizKey, CONSUMER_GROUP, orderNo);
        return PostingResult.created(je.getJeNo());
    }

    public record PostingResult(boolean idempotentHit, String jeNo) {
        static PostingResult created(String jeNo) {
            return new PostingResult(false, jeNo);
        }

        static PostingResult idempotent(String jeNo) {
            return new PostingResult(true, jeNo);
        }
    }
}
