package com.scm.wms.integration;

import com.scm.common.event.EventEnvelope;
import com.scm.spring.event.EventDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "scm.integration.erp-shipment=kafka",
        "scm.integration.tms-handover=off",
        "scm.event.transport=memory"
})
@AutoConfigureMockMvc
class ShipHandoverPublishesEventTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EventDispatcher dispatcher;

    private final List<String> eventTypes = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void subscribe() {
        eventTypes.clear();
        dispatcher.subscribe("WMS_SHIP_HANDOVER", e -> eventTypes.add(e.eventType()));
        dispatcher.subscribe("WMS_OUTBOUND_SHIPPED", e -> eventTypes.add(e.eventType()));
    }

    @Test
    void rfHandoverPublishesKafkaEvents() throws Exception {
        String pkg = "P-ho-" + System.nanoTime();
        String ob = "OB" + pkg.replace("P", "");
        mvc.perform(post("/wms/v1/outbound/create").header("Idempotency-Key", pkg)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"package_no":"%s","source_order_no":"O-ho-1","warehouse_code":"WH-SH-01",
                                 "delivery_type":"EXPRESS","lines":[{"sku_code":"SKU001","qty":"2"}],
                                 "receiver":{"name":"t","phone":"1","province":"z","city":"h","district":"y","address":"a"}}
                                """.formatted(pkg)))
                .andExpect(status().isCreated());

        mvc.perform(post("/rf/v1/ship/handover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"outbound_no":"%s","waybill_no":"WB001","weight_kg":"1.2",
                                 "handover_at":"2026-05-31T12:00:00+08:00"}
                                """.formatted(ob)))
                .andExpect(status().isOk());

        assertTrue(eventTypes.contains("WMS_SHIP_HANDOVER"));
        assertTrue(eventTypes.contains("WMS_OUTBOUND_SHIPPED"));
    }
}
