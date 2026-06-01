package com.scm.spring.event;



import com.fasterxml.jackson.databind.JsonNode;

import com.scm.common.event.EventEnvelope;

import com.scm.common.event.EventJson;



import java.time.OffsetDateTime;

import java.util.UUID;



public final class EventEnvelopeFactory {

    private EventEnvelopeFactory() {

    }



    public static EventEnvelope of(

            String eventType,

            String bizKey,

            String producer,

            String payloadJson) {

        try {

            JsonNode data = EventJson.parseTree(payloadJson);

            return new EventEnvelope(

                    UUID.randomUUID().toString(),

                    eventType,

                    bizKey,

                    1,

                    OffsetDateTime.now(),

                    UUID.randomUUID().toString(),

                    producer,

                    data

            );

        } catch (Exception e) {

            throw new IllegalArgumentException("Invalid payload json", e);

        }

    }

}

