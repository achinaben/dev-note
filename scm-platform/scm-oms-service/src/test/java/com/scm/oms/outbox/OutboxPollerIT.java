package com.scm.oms.outbox;

import com.scm.oms.order.OrderApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Outbox 写入 MySQL 后经 relay 投递 Kafka（Testcontainers）。
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "scm.storage=jdbc",
        "scm.event.transport=kafka",
        "scm.outbox.relay-delay-ms=60000"
})
class OutboxPollerIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("scm_oms")
            .withUsername("test")
            .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("scm.event.kafka-bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    OrderApplicationService orderService;

    @Autowired
    OutboxStore outboxStore;

    @Autowired
    OutboxRelayService outboxRelayService;

    @Test
    void relayMarksOutboxPublishedAfterPay() {
        var line = List.of(Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01"));
        String token = "ct-outbox-" + System.nanoTime();
        String orderNo = orderService.createOrder("U10001", token, line).order().getOrderNo();
        orderService.markPaid(orderNo, "notify-" + token);

        String bizKey = "ORDER_PAID+" + orderNo;
        assertTrue(outboxStore.exists(bizKey));
        org.junit.jupiter.api.Assertions.assertFalse(outboxStore.isPublished(bizKey));

        outboxRelayService.relayPending();

        assertTrue(outboxStore.isPublished(bizKey));
    }
}
