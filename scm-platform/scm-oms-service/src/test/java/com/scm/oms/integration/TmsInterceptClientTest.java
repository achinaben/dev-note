package com.scm.oms.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TmsInterceptClientTest {

    private HttpServer server;
    private final AtomicReference<String> interceptedShipment = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            interceptedShipment.set(exchange.getRequestURI().getPath());
            byte[] ok = "{\"code\":\"0\"}".getBytes();
            exchange.sendResponseHeaders(200, ok.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(ok);
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
    void interceptPostsShipmentNo() {
        TmsInterceptClient client = new TmsInterceptClient();
        org.springframework.test.util.ReflectionTestUtils.setField(
                client, "tmsBaseUrl", "http://localhost:" + server.getAddress().getPort());
        org.springframework.test.util.ReflectionTestUtils.setField(client, "interceptOnAfterSale", true);
        client.interceptPackages(List.of("P-INT-001"));
        assertTrue(interceptedShipment.get().contains("/tms/v1/shipment/SHP-INT-001/intercept"));
    }

    @Test
    void interceptSkipsWhenDisabled() {
        TmsInterceptClient client = new TmsInterceptClient();
        org.springframework.test.util.ReflectionTestUtils.setField(
                client, "tmsBaseUrl", "http://localhost:" + server.getAddress().getPort());
        org.springframework.test.util.ReflectionTestUtils.setField(client, "interceptOnAfterSale", false);
        client.interceptPackages(List.of("P-X"));
        assertNull(interceptedShipment.get());
    }
}
