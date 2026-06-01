package com.scm.wms.inventory.reserve;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class MemoryReserveRepository implements ReserveRepository {

    private final Map<String, OrderReserve> byKey = new ConcurrentHashMap<>();
    private final Map<String, OrderReserve> byOrder = new ConcurrentHashMap<>();

    @Override
    public Optional<OrderReserve> findByIdempotencyKey(String key) {
        return Optional.ofNullable(byKey.get(key));
    }

    @Override
    public Optional<OrderReserve> findByOrderNo(String orderNo) {
        return Optional.ofNullable(byOrder.get(orderNo));
    }

    @Override
    public void save(OrderReserve reserve) {
        byKey.put(reserve.idempotencyKey(), reserve);
        byOrder.put(reserve.orderNo(), reserve);
    }

    @Override
    public void updateStatus(String orderNo, String status) {
        OrderReserve existing = byOrder.get(orderNo);
        if (existing != null) {
            OrderReserve updated = new OrderReserve(
                    existing.orderNo(), existing.idempotencyKey(), status, existing.lines());
            byKey.put(updated.idempotencyKey(), updated);
            byOrder.put(orderNo, updated);
        }
    }
}
