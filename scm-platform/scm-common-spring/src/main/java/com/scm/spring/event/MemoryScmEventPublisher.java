package com.scm.spring.event;



import com.scm.common.event.EventEnvelope;

import com.scm.common.event.EventJson;



public class MemoryScmEventPublisher implements ScmEventPublisher {

    private final EventDispatcher dispatcher;



    public MemoryScmEventPublisher(EventDispatcher dispatcher) {

        this.dispatcher = dispatcher;

    }



    @Override

    public void publish(EventEnvelope envelope) {

        try {

            String json = EventJson.mapper().writeValueAsString(envelope);

            dispatcher.dispatch(json);

        } catch (Exception e) {

            throw new IllegalStateException("Publish failed", e);

        }

    }

}

