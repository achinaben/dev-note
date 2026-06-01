package com.scm.spring.event;



import com.scm.common.event.EventEnvelope;



public interface ScmEventPublisher {

    void publish(EventEnvelope envelope);

}

