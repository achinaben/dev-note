package com.scm.oms.aftersale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryAfterSaleRepository implements AfterSaleRepository {
    private final Map<String, AfterSaleRecord> byNo = new ConcurrentHashMap<>();
    private final Map<String, String> byOrder = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public AfterSaleRecord save(AfterSaleRecord record) {
        if (record.getAfterSaleNo() == null) {
            record.setAfterSaleNo("AS" + String.format("%08d", seq.getAndIncrement()));
        }
        byNo.put(record.getAfterSaleNo(), record);
        byOrder.put(record.getOrderNo(), record.getAfterSaleNo());
        return record;
    }

    @Override
    public Optional<AfterSaleRecord> findByOrderNo(String orderNo) {
        String no = byOrder.get(orderNo);
        return no == null ? Optional.empty() : Optional.ofNullable(byNo.get(no));
    }

    @Override
    public Optional<AfterSaleRecord> findByAfterSaleNo(String afterSaleNo) {
        return Optional.ofNullable(byNo.get(afterSaleNo));
    }

    @Override
    public void update(AfterSaleRecord record) {
        byNo.put(record.getAfterSaleNo(), record);
    }
}
