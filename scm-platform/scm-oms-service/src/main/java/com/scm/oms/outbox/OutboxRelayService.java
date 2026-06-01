package com.scm.oms.outbox;

import com.scm.spring.event.EventEnvelopeFactory;
import com.scm.spring.event.ScmEventPublisher;
import com.scm.spring.event.ScmEventProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class OutboxRelayService {
    private final OutboxStore outboxStore;
    private final ScmEventPublisher eventPublisher;
    private final ScmEventProperties eventProperties;

    public OutboxRelayService(
            OutboxStore outboxStore,
            ScmEventPublisher eventPublisher,
            ScmEventProperties eventProperties) {
        this.outboxStore = outboxStore;
        this.eventPublisher = eventPublisher;
        this.eventProperties = eventProperties;
    }

    @Scheduled(fixedDelayString = "${scm.outbox.relay-delay-ms:300}")
    public void relayPending() {
        for (OutboxEvent event : outboxStore.pending()) {
            eventPublisher.publish(EventEnvelopeFactory.of(
                    event.eventType(),
                    event.bizKey(),
                    eventProperties.getProducer(),
                    event.payloadJson()
            ));
            outboxStore.markPublished(event.bizKey());
        }
    }
}
