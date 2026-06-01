package com.scm.oms.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryOutboxStore implements OutboxStore {
    private final List<OutboxEvent> events = new CopyOnWriteArrayList<>();
    private final Set<String> publishedKeys = ConcurrentHashMap.newKeySet();

    @Override
    public void append(OutboxEvent event) {
        events.add(event);
    }

    @Override
    public boolean exists(String bizKey) {
        return events.stream().anyMatch(e -> e.bizKey().equals(bizKey));
    }

    @Override
    public boolean isPublished(String bizKey) {
        return publishedKeys.contains(bizKey);
    }

    @Override
    public List<OutboxEvent> pending() {
        return events.stream()
                .filter(e -> !publishedKeys.contains(e.bizKey()))
                .toList();
    }

    @Override
    public void markPublished(String bizKey) {
        publishedKeys.add(bizKey);
    }

    @Override
    public List<OutboxEvent> all() {
        return List.copyOf(events);
    }
}
