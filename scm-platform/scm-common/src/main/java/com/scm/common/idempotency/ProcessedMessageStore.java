package com.scm.common.idempotency;

public interface ProcessedMessageStore {
    boolean exists(String bizKey, String consumerGroup);

    void markProcessed(String bizKey, String consumerGroup, String eventId);
}
