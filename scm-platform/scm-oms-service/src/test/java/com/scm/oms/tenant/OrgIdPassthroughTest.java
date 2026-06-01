package com.scm.oms.tenant;

import com.scm.common.tenant.OrgIdContext;
import com.scm.oms.order.OrderApplicationService;
import com.scm.oms.outbox.OutboxStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OrgIdPassthroughTest {

    @Autowired
    OrderApplicationService orderService;

    @Autowired
    OutboxStore outboxStore;

    @Test
    void orderPaidOutboxCarriesOrgId() {
        String token = "ct-org-" + System.nanoTime();
        var created = orderService.createOrder("U10001", token,
                List.of(Map.of("sku_id", "SKU001", "qty", "2", "warehouse_id", "WH-SH-01")));
        String orderNo = created.order().getOrderNo();

        OrgIdContext.set("ORG002");
        try {
            orderService.markPaid(orderNo, "notify-" + token);
        } finally {
            OrgIdContext.clear();
        }

        String bizKey = "ORDER_PAID+" + orderNo;
        assertTrue(outboxStore.all().stream()
                .anyMatch(e -> e.bizKey().equals(bizKey) && e.payloadJson().contains("\"org_id\":\"ORG002\"")));
    }
}
