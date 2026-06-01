package com.scm.common.event;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.scm.common.schema.SchemaValidator;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventEnvelopeJsonTest {

    @Test
    void parseOrderPaidFixture() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/order-paid.json")) {
            var tree = EventJson.parseTree(new String(in.readAllBytes()));
            try (InputStream schema = getClass().getResourceAsStream("/contracts/event-envelope.schema.json")) {
                new SchemaValidator(schema).validateOrThrow(tree);
            }
            EventEnvelope env = EventJson.parse(tree.toString());
            assertEquals("ORDER_PAID", env.eventType());
            assertEquals("ORDER_PAID+O-fix-001", env.bizKey());
        }
    }

    @Test
    void processedMessageIdempotent() {
        var store = new com.scm.common.idempotency.InMemoryProcessedMessageStore();
        assertFalse(store.exists("ORDER_PAID+O-fix-001", "wms-cg"));
        store.markProcessed("ORDER_PAID+O-fix-001", "wms-cg", "E-fix-001");
        assertTrue(store.exists("ORDER_PAID+O-fix-001", "wms-cg"));
    }
}
