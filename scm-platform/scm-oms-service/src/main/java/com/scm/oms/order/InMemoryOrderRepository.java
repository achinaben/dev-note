package com.scm.oms.order;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, OrderRecord> byOrderNo = new ConcurrentHashMap<>();
    private final Map<String, String> orderNoByClientToken = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public Optional<OrderRecord> findByClientToken(String buyerId, String clientToken) {
        String orderNo = orderNoByClientToken.get(buyerId + "::" + clientToken);
        if (orderNo == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byOrderNo.get(orderNo));
    }

    @Override
    public Optional<OrderRecord> findByOrderNo(String orderNo) {
        return Optional.ofNullable(byOrderNo.get(orderNo));
    }

    @Override
    public OrderRecord saveNew(OrderRecord order) {
        if (order.getOrderNo() == null) {
            long n = seq.getAndIncrement();
            order.setOrderNo("O20260531" + String.format("%06d", n));
            order.setTradeNo("T20260531" + String.format("%06d", n));
        }
        order.setVersion(1);
        byOrderNo.put(order.getOrderNo(), order);
        orderNoByClientToken.put(order.getBuyerId() + "::" + order.getClientToken(), order.getOrderNo());
        return order;
    }

    @Override
    public void update(OrderRecord order) {
        byOrderNo.put(order.getOrderNo(), order);
    }

    @Override
    public long countByBuyerAndToken(String buyerId, String clientToken) {
        return findByClientToken(buyerId, clientToken).isPresent() ? 1 : 0;
    }
}
