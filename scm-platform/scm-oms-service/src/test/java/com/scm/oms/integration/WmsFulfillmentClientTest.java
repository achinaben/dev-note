package com.scm.oms.integration;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WmsFulfillmentClientTest {

    private HttpServer server;
    private final AtomicInteger byOrderCalls = new AtomicInteger();
    private final AtomicInteger createCalls = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/wms/v1/outbound/by-order/O1", exchange -> {
            byOrderCalls.incrementAndGet();
            write(exchange, 200, "{\"outbound_no\":\"OB-P1\"}");
        });
        server.createContext("/wms/v1/outbound/by-package/P2", exchange ->
                write(exchange, 404, ""));
        server.createContext("/wms/v1/outbound/create", exchange -> {
            createCalls.incrementAndGet();
            write(exchange, 201, "{\"outbound_no\":\"OB-P2\"}");
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
    void createOutboundChecksExistingPackageNotExistingOrder() {
        WmsFulfillmentClient client = new WmsFulfillmentClient();
        ReflectionTestUtils.setField(
                client, "wmsBaseUrl", "http://localhost:" + server.getAddress().getPort());

        String outboundNo = client.createOutbound("P2", "O1");

        assertEquals("OB-P2", outboundNo);
        assertEquals(0, byOrderCalls.get());
        assertEquals(1, createCalls.get());
    }

    private static void write(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
