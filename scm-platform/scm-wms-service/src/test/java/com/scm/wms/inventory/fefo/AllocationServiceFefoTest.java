package com.scm.wms.inventory.fefo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllocationServiceFefoTest {

    @Test
    void picksEarliestExpiryFirst() {
        var lots = List.of(
                new StockLot("L-late", "WH-SH-01", "SKU001", 10, LocalDate.of(2026, 12, 1)),
                new StockLot("L-early", "WH-SH-01", "SKU001", 2, LocalDate.of(2026, 5, 1)),
                new StockLot("L-mid", "WH-SH-01", "SKU001", 5, LocalDate.of(2026, 8, 1))
        );
        List<AllocationLine> lines = FefoAllocator.allocate(lots, 4);
        assertEquals(2, lines.size());
        assertEquals("L-early", lines.get(0).lotId());
        assertEquals(2, lines.get(0).qty());
        assertEquals("L-mid", lines.get(1).lotId());
        assertEquals(2, lines.get(1).qty());
    }

    @Test
    void throwsWhenNotEnoughStock() {
        var lots = List.of(new StockLot("L1", "WH-SH-01", "SKU001", 1, LocalDate.now()));
        assertThrows(InsufficientStockException.class, () -> FefoAllocator.allocate(lots, 2));
    }
}
