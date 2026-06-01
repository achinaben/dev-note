package com.scm.tms.track;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "scm.storage=jdbc")
class TmsTrackEventJdbcIT {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("scm_tms")
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
    TrackEventRepository trackEventRepository;

    @Test
    void appendPersistsAcrossCalls() {
        String wb = "WB-jdbc-trk-" + System.nanoTime();
        trackEventRepository.append(wb, "WMS_HANDOVER", "wms");
        trackEventRepository.append(wb, "IN_TRANSIT", "carrier");
        var events = trackEventRepository.listByWaybill(wb);
        assertFalse(events.isEmpty());
        assertEquals(2, events.size());
        assertEquals("WMS_HANDOVER", events.get(0).getEventCode());
    }
}
