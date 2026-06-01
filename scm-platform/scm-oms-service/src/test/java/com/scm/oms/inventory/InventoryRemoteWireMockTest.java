package com.scm.oms.inventory;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InventoryRemoteWireMockTest {

    private static HttpServer server;
    private static final AtomicInteger reserveCalls = new AtomicInteger();
    private static final AtomicInteger confirmCalls = new AtomicInteger();

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/inventory/v1/reserve", exchange -> {
            reserveCalls.incrementAndGet();
            respond(exchange, "{\"reserve_id\":\"RSV-1\",\"status\":\"RESERVED\"}");
        });
        server.createContext("/inventory/v1/confirm", exchange -> {
            confirmCalls.incrementAndGet();
            respond(exchange, "{\"status\":\"CONFIRMED\"}");
        });
        server.start();
    }

    @AfterAll
    static void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @DynamicPropertySource
    static void remoteInventory(DynamicPropertyRegistry registry) {
        registry.add("scm.inventory.provider", () -> "mock");
        registry.add("scm.inventory.remote-enabled", () -> "true");
        registry.add("scm.inventory.base-url",
                () -> "http://127.0.0.1:" + server.getAddress().getPort() + "/inventory/v1");
    }

    @Autowired
    InventoryService inventoryService;

    @Test
    void reserveAndConfirmCallRemoteApi() {
        inventoryService.reserve("O-inv-wm-1");
        inventoryService.confirm("O-inv-wm-1");
        assertEquals("CONFIRMED", inventoryService.status("O-inv-wm-1"));
        assertEquals(1, reserveCalls.get());
        assertEquals(1, confirmCalls.get());
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
