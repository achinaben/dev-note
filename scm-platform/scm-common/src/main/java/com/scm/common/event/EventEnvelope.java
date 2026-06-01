package com.scm.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("biz_key") String bizKey,
        @JsonProperty("schema_version") int schemaVersion,
        @JsonProperty("occurred_at") OffsetDateTime occurredAt,
        @JsonProperty("trace_id") String traceId,
        @JsonProperty("producer") String producer,
        @JsonProperty("data") JsonNode data
) {
}
