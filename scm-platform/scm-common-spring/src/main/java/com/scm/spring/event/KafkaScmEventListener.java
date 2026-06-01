package com.scm.spring.event;



import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;



@Component

@ConditionalOnProperty(name = "scm.event.transport", havingValue = "kafka")

public class KafkaScmEventListener {

    private final EventDispatcher dispatcher;



    public KafkaScmEventListener(EventDispatcher dispatcher) {

        this.dispatcher = dispatcher;

    }



    @KafkaListener(topics = {

            ScmEventTopics.ORDER_LIFECYCLE,

            ScmEventTopics.WMS_OUTBOUND

    }, groupId = "${spring.application.name:scm}-events")

    public void onMessage(String payload) {

        dispatcher.dispatch(payload);

    }

}

