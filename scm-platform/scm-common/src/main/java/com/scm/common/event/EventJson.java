package com.scm.common.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class EventJson {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private EventJson() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static EventEnvelope parse(String json) throws Exception {
        return MAPPER.readValue(json, EventEnvelope.class);
    }

    public static JsonNode parseTree(String json) throws Exception {
        return MAPPER.readTree(json);
    }
}
