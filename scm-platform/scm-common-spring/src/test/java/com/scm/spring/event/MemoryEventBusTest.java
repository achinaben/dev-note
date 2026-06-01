package com.scm.spring.event;



import org.junit.jupiter.api.Test;



import java.util.concurrent.atomic.AtomicInteger;



import static org.junit.jupiter.api.Assertions.assertEquals;



class MemoryEventBusTest {

    @Test

    void dispatchesToSubscriber() {

        EventDispatcher dispatcher = new EventDispatcher();

        MemoryScmEventPublisher publisher = new MemoryScmEventPublisher(dispatcher);

        AtomicInteger count = new AtomicInteger();

        dispatcher.subscribe("ORDER_PAID", e -> count.incrementAndGet());

        publisher.publish(EventEnvelopeFactory.of(

                "ORDER_PAID",

                "ORDER_PAID+O1",

                "oms",

                "{\"order_no\":\"O1\"}"

        ));

        assertEquals(1, count.get());

    }

}

