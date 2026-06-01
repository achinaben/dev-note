package com.scm.common.idempotency;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProcessedMessageStore implements ProcessedMessageStore {
    private final Set<String> keys = ConcurrentHashMap.newKeySet();

    private static String key(String bizKey, String consumerGroup) {
        return consumerGroup + "::" + bizKey;
    }

    @Override
    public boolean exists(String bizKey, String consumerGroup) {
        return keys.contains(key(bizKey, consumerGroup));
    }

    @Override
    public void markProcessed(String bizKey, String consumerGroup, String eventId) {
        keys.add(key(bizKey, consumerGroup));
    }
}
