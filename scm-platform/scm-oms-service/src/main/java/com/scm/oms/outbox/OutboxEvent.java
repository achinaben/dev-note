package com.scm.oms.outbox;

import java.time.OffsetDateTime;

public record OutboxEvent(
        String eventType,
        String bizKey,
        OffsetDateTime occurredAt,
        String payloadJson
) {
}
