package com.scm.wms.inventory;

import com.scm.wms.inventory.fefo.InsufficientStockException;
import com.scm.wms.inventory.fefo.StockLot;
import com.scm.wms.inventory.stock.MemoryStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AllocationConcurrentTest {

    @Autowired
    InventoryAllocationService allocationService;

    @Autowired
    MemoryStockRepository stockRepository;

    @BeforeEach
    void resetStock() {
        stockRepository.replaceAllForTest(List.of(
                new StockLot("L-only", "WH-SH-01", "SKU001", 5, LocalDate.of(2026, 6, 1))));
    }

    @Test
    void concurrentReserveNeverGoesNegative() throws InterruptedException {
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            int idx = i;
            pool.submit(() -> {
                try {
                    start.await();
                    String orderNo = "O-conc-" + idx;
                    allocationService.reserve(orderNo, Map.of(
                            "client_token", "ct-" + idx,
                            "order_no", orderNo,
                            "warehouse_id", "WH-SH-01",
                            "lines", List.of(Map.of("sku_id", "SKU001", "qty", "1"))));
                    success.incrementAndGet();
                } catch (InsufficientStockException | IllegalStateException e) {
                    conflict.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();

        assertEquals(5, success.get());
        assertEquals(5, conflict.get());
        assertEquals(0, stockRepository.totalAvailable("WH-SH-01", "SKU001"));
    }
}
