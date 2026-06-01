package com.scm.oms.inventory;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("wms-inventory")
class InventoryWmsProviderTest {

    private static HttpServer server;
    private static final AtomicInteger reserveCalls = new AtomicInteger();

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/inventory/v1/reserve", exchange -> {
            reserveCalls.incrementAndGet();
            respond(exchange, "{\"reserve_id\":\"RSV-wms\",\"status\":\"RESERVED\"}");
        });
        server.createContext("/inventory/v1/confirm", exchange ->
                respond(exchange, "{\"status\":\"CONFIRMED\"}"));
        server.start();
    }

    @AfterAll
    static void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @DynamicPropertySource
    static void wmsInventory(DynamicPropertyRegistry registry) {
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        registry.add("scm.inventory.wms-base-url", () -> base);
    }

    @Autowired
    InventoryService inventoryService;

    @Test
    void wmsProviderCallsWmsBaseUrl() {
        assertEquals(InventoryProvider.WMS, inventoryService.provider());
        inventoryService.reserve("O-wms-prov-1");
        inventoryService.confirm("O-wms-prov-1");
        assertEquals("CONFIRMED", inventoryService.status("O-wms-prov-1"));
        assertEquals(1, reserveCalls.get());
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
