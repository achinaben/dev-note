package com.scm.oms.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryPaymentNotifyStore implements PaymentNotifyStore {
    private final Map<String, AtomicInteger> successByOrder = new ConcurrentHashMap<>();

    @Override
    public void recordSuccess(String orderNo, String notifyId) {
        successByOrder.computeIfAbsent(orderNo, k -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public int successCount(String orderNo) {
        AtomicInteger c = successByOrder.get(orderNo);
        return c == null ? 0 : c.get();
    }
}
