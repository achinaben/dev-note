package com.scm.oms.event;

import com.scm.oms.order.OrderApplicationService;
import com.scm.oms.order.OrderStatus;
import com.scm.spring.event.EventDispatcher;
import com.scm.spring.event.EventEnvelopeFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WmsOutboundShippedHandlerTest {

    @Autowired
    OrderApplicationService orderService;

    @Autowired
    EventDispatcher dispatcher;

    @Test
    void wmsShippedEventMarksOrderShipped() {
        var line = List.of(Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01"));
        String token = "ct-wms-ev-" + System.nanoTime();
        String orderNo = orderService.createOrder("U10001", token, line).order().getOrderNo();
        orderService.markPaid(orderNo, "notify-" + token);

        String packageNo = "P" + orderNo;
        String outboundNo = "OB" + orderNo;
        String payload = """
                {"outbound_no":"%s","source_order_no":"%s","package_no":"%s",
                 "org_id":"ORG001","wh_code":"WH-SH-01"}
                """.formatted(outboundNo, orderNo, packageNo);
        dispatcher.dispatch(EventEnvelopeFactory.of(
                "WMS_OUTBOUND_SHIPPED",
                "WMS_OUTBOUND_SHIPPED+" + outboundNo,
                "wms",
                payload));

        assertEquals(OrderStatus.SHIPPED, orderService.get(orderNo).getStatus());
    }
}
