package com.scm.oms.aftersale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "scm.storage=jdbc")
class AfterSaleJdbcIT {
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
    AfterSaleService afterSaleService;

    @Autowired
    com.scm.oms.order.OrderApplicationService orderService;

    @Autowired
    com.scm.oms.fulfillment.FulfillmentService fulfillmentService;

    @Test
    void applyAndApprovePersisted() {
        var line = java.util.List.of(
                java.util.Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01"));
        String token = "ct-as-jdbc-" + System.nanoTime();
        String orderNo = orderService.createOrder("U10001", token, line).order().getOrderNo();
        orderService.markPaid(orderNo, "notify-" + token);
        fulfillmentService.markShipped(orderNo);
        var applied = afterSaleService.applyReturn(orderNo);
        afterSaleService.approve(applied.getAfterSaleNo());
        assertEquals("APPROVED", afterSaleService.status(orderNo));
    }
}
