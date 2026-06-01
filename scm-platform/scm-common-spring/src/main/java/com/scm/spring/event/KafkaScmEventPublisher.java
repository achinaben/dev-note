package com.scm.spring.event;



import com.scm.common.event.EventEnvelope;

import com.scm.common.event.EventJson;

import org.springframework.kafka.core.KafkaTemplate;



public class KafkaScmEventPublisher implements ScmEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;



    public KafkaScmEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;

    }



    @Override

    public void publish(EventEnvelope envelope) {

        try {

            String topic = ScmEventTopics.topicFor(envelope.eventType());

            String json = EventJson.mapper().writeValueAsString(envelope);

            kafkaTemplate.send(topic, envelope.bizKey(), json);

        } catch (Exception e) {

            throw new IllegalStateException("Kafka publish failed", e);

        }

    }

}

