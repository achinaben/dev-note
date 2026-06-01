package com.scm.erp.fi;

import com.scm.erp.integration.WmsShipmentPostingService;
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
class ErpJournalWaybillJdbcIT {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("scm_erp")
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
    WmsShipmentPostingService postingService;

    @Autowired
    JournalRepository journalRepository;

    @Test
    void waybillPersistedInJdbc() {
        String bizKey = "WMS_OUTBOUND_SHIPPED+OB-jdbc-erp-" + System.nanoTime();
        postingService.postShipment(bizKey, new WmsShipmentPostingService.WmsShipmentRequest(
                "OB-jdbc-erp",
                "O-jdbc-erp",
                "ORG001",
                "WH-SH-01",
                List.of(Map.of("material_code", "M001", "qty", "2")),
                "WB-JDBC-ERP"));
        assertEquals("WB-JDBC-ERP", journalRepository.findByBizKey(bizKey).orElseThrow().getWaybillNo());
    }
}
