package com.scm.spring.event;



import com.scm.common.event.EventEnvelope;

import com.scm.common.event.EventJson;



import java.util.ArrayList;

import java.util.List;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.Consumer;



public class EventDispatcher {

    private final Map<String, List<Consumer<EventEnvelope>>> handlers = new ConcurrentHashMap<>();



    public void subscribe(String eventType, Consumer<EventEnvelope> handler) {

        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);

    }



    public void dispatch(String json) {

        try {

            EventEnvelope envelope = EventJson.parse(json);

            dispatch(envelope);

        } catch (Exception e) {

            throw new IllegalStateException("Invalid event json", e);

        }

    }



    public void dispatch(EventEnvelope envelope) {

        List<Consumer<EventEnvelope>> list = handlers.get(envelope.eventType());

        if (list == null) {

            return;

        }

        for (Consumer<EventEnvelope> h : list) {

            h.accept(envelope);

        }

    }

}

