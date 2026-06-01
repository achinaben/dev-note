package com.scm.oms.outbox;

import java.util.List;

public interface OutboxStore {
    void append(OutboxEvent event);

    boolean exists(String bizKey);

    boolean isPublished(String bizKey);

    List<OutboxEvent> pending();

    void markPublished(String bizKey);

    List<OutboxEvent> all();
}
