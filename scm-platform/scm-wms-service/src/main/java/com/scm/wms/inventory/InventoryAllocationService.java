package com.scm.wms.inventory;

import com.scm.wms.inventory.fefo.AllocationLine;
import com.scm.wms.inventory.fefo.InsufficientStockException;
import com.scm.wms.inventory.reserve.OrderReserve;
import com.scm.wms.inventory.reserve.ReserveRepository;
import com.scm.wms.inventory.stock.StockRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InventoryAllocationService {

    private final StockRepository stockRepository;
    private final ReserveRepository reserveRepository;
    private final InventoryTxRunner txRunner;

    public InventoryAllocationService(
            StockRepository stockRepository,
            ReserveRepository reserveRepository,
            InventoryTxRunner txRunner) {
        this.stockRepository = stockRepository;
        this.reserveRepository = reserveRepository;
        this.txRunner = txRunner;
    }

    @PostConstruct
    void seedStock() {
        stockRepository.seedDemoLotsIfEmpty();
    }

    public Map<String, Object> reserve(String idempotencyKey, Map<String, Object> body) {
        return txRunner.run(() -> doReserve(idempotencyKey, body));
    }

    private Map<String, Object> doReserve(String idempotencyKey, Map<String, Object> body) {
        Optional<OrderReserve> existing = reserveRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return reserveResponse(existing.get());
        }
        String orderNo = String.valueOf(body.get("order_no"));
        if (reserveRepository.findByOrderNo(orderNo).isPresent()) {
            return reserveRepository.findByOrderNo(orderNo).map(this::reserveResponse).orElseThrow();
        }
        String warehouseId = String.valueOf(body.get("warehouse_id"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineMaps = (List<Map<String, Object>>) body.get("lines");
        List<AllocationLine> allocations = new ArrayList<>();
        for (Map<String, Object> line : lineMaps) {
            String skuId = String.valueOf(line.get("sku_id"));
            int qty = Integer.parseInt(String.valueOf(line.get("qty")));
            allocations.addAll(stockRepository.reserveSku(warehouseId, skuId, qty));
        }
        OrderReserve reserve = new OrderReserve(orderNo, idempotencyKey, "RESERVED", List.copyOf(allocations));
        reserveRepository.save(reserve);
        return reserveResponse(reserve);
    }

    public Map<String, Object> confirm(Map<String, String> body) {
        return txRunner.run(() -> {
            String orderNo = body.get("order_no");
            reserveRepository.updateStatus(orderNo, "CONFIRMED");
            return Map.of("order_no", orderNo, "status", "CONFIRMED");
        });
    }

    public Map<String, Object> release(Map<String, String> body) {
        return txRunner.run(() -> {
            String orderNo = body.get("order_no");
            OrderReserve reserve = reserveRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new IllegalArgumentException("无预占记录: " + orderNo));
            if (!"RELEASED".equals(reserve.status())) {
                stockRepository.restore(reserve.lines());
                reserveRepository.updateStatus(orderNo, "RELEASED");
            }
            return Map.of("order_no", orderNo, "status", "RELEASED");
        });
    }

    public Map<String, Object> status(String orderNo) {
        return reserveRepository.findByOrderNo(orderNo)
                .map(r -> Map.<String, Object>of("order_no", orderNo, "status", r.status()))
                .orElse(Map.of("order_no", orderNo, "status", "NONE"));
    }

    private Map<String, Object> reserveResponse(OrderReserve reserve) {
        return Map.of(
                "reserve_id", "RSV-" + reserve.idempotencyKey(),
                "order_no", reserve.orderNo(),
                "status", reserve.status());
    }
}
