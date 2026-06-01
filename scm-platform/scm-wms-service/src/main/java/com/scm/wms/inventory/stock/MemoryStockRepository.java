package com.scm.wms.inventory.stock;

import com.scm.wms.inventory.fefo.AllocationLine;
import com.scm.wms.inventory.fefo.FefoAllocator;
import com.scm.wms.inventory.fefo.StockLot;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class MemoryStockRepository implements StockRepository {

    private final Map<String, StockLot> lots = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        seedDemoLotsIfEmpty();
    }

    @Override
    public List<StockLot> findAvailableLots(String warehouseId, String skuId) {
        synchronized (lock) {
            return lots.values().stream()
                    .filter(l -> l.warehouseId().equals(warehouseId) && l.skuId().equals(skuId) && l.qtyAvailable() > 0)
                    .map(l -> new StockLot(l.lotId(), l.warehouseId(), l.skuId(), l.qtyAvailable(), l.expireDate()))
                    .toList();
        }
    }

    @Override
    public List<AllocationLine> reserveSku(String warehouseId, String skuId, int qty) {
        synchronized (lock) {
            List<AllocationLine> lines = FefoAllocator.allocate(findAvailableLots(warehouseId, skuId), qty);
            applyDeductionsUnlocked(lines);
            return lines;
        }
    }

    @Override
    public void applyDeductions(List<AllocationLine> lines) {
        synchronized (lock) {
            applyDeductionsUnlocked(lines);
        }
    }

    private void applyDeductionsUnlocked(List<AllocationLine> lines) {
        for (AllocationLine line : lines) {
            StockLot lot = lots.get(line.lotId());
            if (lot == null || lot.qtyAvailable() < line.qty()) {
                throw new IllegalStateException("库存扣减失败 lot=" + line.lotId());
            }
            lots.put(line.lotId(), new StockLot(
                    lot.lotId(), lot.warehouseId(), lot.skuId(),
                    lot.qtyAvailable() - line.qty(), lot.expireDate()));
        }
    }

    @Override
    public void restore(List<AllocationLine> lines) {
        synchronized (lock) {
            for (AllocationLine line : lines) {
                StockLot lot = lots.get(line.lotId());
                if (lot == null) {
                    continue;
                }
                lots.put(line.lotId(), new StockLot(
                        lot.lotId(), lot.warehouseId(), lot.skuId(),
                        lot.qtyAvailable() + line.qty(), lot.expireDate()));
            }
        }
    }

    @Override
    public void seedDemoLotsIfEmpty() {
        synchronized (lock) {
            if (!lots.isEmpty()) {
                return;
            }
            putLot("LOT-A", "WH-SH-01", "SKU001", 3, LocalDate.of(2026, 6, 1));
            putLot("LOT-B", "WH-SH-01", "SKU001", 5, LocalDate.of(2026, 7, 1));
            putLot("LOT-C", "WH-SH-01", "SKU001", 10, LocalDate.of(2026, 8, 1));
        }
    }

    private void putLot(String lotId, String wh, String sku, int qty, LocalDate expire) {
        lots.put(lotId, new StockLot(lotId, wh, sku, qty, expire));
    }

    public int totalAvailable(String warehouseId, String skuId) {
        synchronized (lock) {
            return findAvailableLots(warehouseId, skuId).stream().mapToInt(StockLot::qtyAvailable).sum();
        }
    }

    /** 单测并发场景重置库存 */
    public void replaceAllForTest(List<StockLot> seed) {
        synchronized (lock) {
            lots.clear();
            for (StockLot lot : seed) {
                lots.put(lot.lotId(), lot);
            }
        }
    }
}
