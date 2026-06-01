package com.scm.oms.order;

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
class OmsOrderJdbcIT {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("scm_oms")
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
    OrderApplicationService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void idempotentClientTokenOneRow() {
        var line = List.of(Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01"));
        String token = "ct-jdbc-" + System.nanoTime();
        orderService.createOrder("U10001", token, line);
        orderService.createOrder("U10001", token, line);
        assertEquals(1, orderRepository.countByBuyerAndToken("U10001", token));
    }
}
