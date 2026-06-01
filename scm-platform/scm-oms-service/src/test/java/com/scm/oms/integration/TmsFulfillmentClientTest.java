package com.scm.oms.integration;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TmsFulfillmentClientTest {

    private HttpServer server;
    private final AtomicInteger deliverCalls = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/tms/v1/shipment/create", exchange -> {
            byte[] body = """
                    {"code":"TMS_10001","message":"Idempotent replay","data":{"shipment_no":"SHP1"}}
                    """.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(409, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/tms/v1/integration/deliver", exchange -> {
            deliverCalls.incrementAndGet();
            byte[] body = "{\"code\":\"0\"}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createAndDeliverContinuesWhenShipmentCreateIsIdempotentReplay() {
        TmsFulfillmentClient client = new TmsFulfillmentClient(new TmsFreightClient());
        ReflectionTestUtils.setField(
                client, "tmsBaseUrl", "http://localhost:" + server.getAddress().getPort());
        ReflectionTestUtils.setField(client, "useRecommendedCarrier", false);

        assertDoesNotThrow(() -> client.createAndDeliver("P1", "O1"));

        assertEquals(1, deliverCalls.get());
    }
}
