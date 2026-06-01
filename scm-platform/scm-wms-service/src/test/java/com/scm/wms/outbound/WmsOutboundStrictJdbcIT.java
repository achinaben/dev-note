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
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "scm.storage=jdbc",
        "scm.wms.outbound.relaxed-handover=false"
})
class WmsOutboundStrictJdbcIT {
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

    @Autowired
    OutboundShipmentService shipmentService;

    @Test
    void strictFlowPickCheckHandover() {
        String pkg = "P-strict-jdbc-" + System.nanoTime();
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("package_no", pkg);
        req.put("source_order_no", "O-strict-jdbc");
        req.put("warehouse_code", "WH-SH-01");
        req.put("delivery_type", "EXPRESS");
        req.put("lines", List.of(Map.of("sku_code", "SKU001", "qty", "2")));
        req.put("receiver", Map.of(
                "name", "t", "phone", "1", "province", "z", "city", "h", "district", "y", "address", "a"));
        var created = outboundService.create(pkg, req);
        String ob = created.outboundNo();

        assertThrows(IllegalStateException.class, () ->
                shipmentService.handover(ob, List.of(Map.of("sku_code", "SKU001", "qty", "2")),
                        Map.of("waybill_no", "WB-X")));

        shipmentService.confirmPick(ob);
        assertEquals("PICKED", shipmentService.find(ob).orElseThrow().getStatus());

        shipmentService.confirmCheck(ob);
        assertEquals("CHECKED", shipmentService.find(ob).orElseThrow().getStatus());

        var handover = shipmentService.handover(ob,
                List.of(Map.of("sku_code", "SKU001", "qty", "2")),
                Map.of("waybill_no", "WB-STRICT-JDBC"));
        assertEquals("SHIPPED", handover.get("status"));
        assertEquals("SHIPPED", shipmentService.find(ob).orElseThrow().getStatus());
    }
}
