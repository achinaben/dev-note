package com.scm.wms.outbound;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "scm.storage=jdbc")
class WmsOutboundJdbcIT {
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
    OutboundApplicationService outboundService;

    @Test
    void idempotentPackageOneRow() {
        String pkg = "P-jdbc-wms-" + System.nanoTime();
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("package_no", pkg);
        req.put("source_order_no", "O-jdbc-wms");
        req.put("warehouse_code", "WH-SH-01");
        req.put("delivery_type", "EXPRESS");
        req.put("lines", List.of(Map.of("sku_code", "SKU001", "qty", "2")));
        req.put("receiver", Map.of(
                "name", "t", "phone", "1", "province", "z", "city", "h", "district", "y", "address", "a"));
        outboundService.create(pkg, req);
        outboundService.create(pkg, req);
        assertEquals(1, outboundService.countByPackageNo(pkg));
    }
}
