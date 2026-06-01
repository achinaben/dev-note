package com.scm.wms.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "scm.storage=jdbc")
class WmsInventoryJdbcIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("scm_wms")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    InventoryAllocationService allocationService;

    @Test
    void reserveConfirmPersistsInJdbc() {
        String orderNo = "O-jdbc-inv-" + System.nanoTime();
        allocationService.reserve(orderNo, Map.of(
                "client_token", "ct-jdbc",
                "order_no", orderNo,
                "warehouse_id", "WH-SH-01",
                "lines", List.of(Map.of("sku_id", "SKU001", "qty", "2"))));
        assertEquals("RESERVED", allocationService.status(orderNo).get("status"));
        allocationService.confirm(Map.of("order_no", orderNo, "reserve_id", "RSV-" + orderNo));
        assertEquals("CONFIRMED", allocationService.status(orderNo).get("status"));
    }
}
