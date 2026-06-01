package com.scm.wms.inventory.fefo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class FefoAllocator {

    private FefoAllocator() {
    }

    /**
     * 按效期升序（先到期先出）分配数量。
     */
    public static List<AllocationLine> allocate(List<StockLot> lots, int qtyNeeded) {
        if (qtyNeeded <= 0) {
            return List.of();
        }
        List<StockLot> sorted = lots.stream()
                .filter(l -> l.qtyAvailable() > 0)
                .sorted(Comparator.comparing(StockLot::expireDate).thenComparing(StockLot::lotId))
                .toList();
        int remaining = qtyNeeded;
        List<AllocationLine> lines = new ArrayList<>();
        for (StockLot lot : sorted) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(remaining, lot.qtyAvailable());
            if (take > 0) {
                lines.add(new AllocationLine(lot.lotId(), take));
                remaining -= take;
            }
        }
        if (remaining > 0) {
            throw new InsufficientStockException("可用库存不足，缺口=" + remaining);
        }
        return List.copyOf(lines);
    }
}
