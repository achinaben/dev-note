package com.scm.erp.fi;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostingEngineTest {
    @Test
    void outboundCostBalanced() {
        PostingEngine engine = new PostingEngine();
        BigDecimal cost = engine.computeOutboundCost(List.of(
                Map.of("material_code", "M001", "qty", "2.0000")
        ));
        assertEquals(0, cost.compareTo(new BigDecimal("100.0000")));
        var je = engine.postOutboundCost("WMS_OUTBOUND_SHIPPED+OB1", cost);
        BigDecimal d = je.lines().stream().map(JournalLineRecord::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal c = je.lines().stream().map(JournalLineRecord::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(d, c);
    }
}
