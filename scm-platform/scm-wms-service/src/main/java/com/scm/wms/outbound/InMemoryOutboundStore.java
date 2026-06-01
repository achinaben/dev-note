package com.scm.wms.outbound;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryOutboundStore implements OutboundStore {
    private final Map<String, OutboundRecord> byPackage = new ConcurrentHashMap<>();
    private final Map<String, OutboundRecord> byOutbound = new ConcurrentHashMap<>();
    private final Map<String, String> orderToPackage = new ConcurrentHashMap<>();

    @Override
    public Optional<OutboundRecord> findByPackageNo(String packageNo) {
        return Optional.ofNullable(byPackage.get(packageNo));
    }

    @Override
    public Optional<OutboundRecord> findByOutboundNo(String outboundNo) {
        return Optional.ofNullable(byOutbound.get(outboundNo));
    }

    @Override
    public Optional<String> findOutboundNoBySourceOrder(String sourceOrderNo) {
        String pkg = orderToPackage.get(sourceOrderNo);
        if (pkg == null) {
            return Optional.empty();
        }
        return findByPackageNo(pkg).map(OutboundRecord::getOutboundNo);
    }

    @Override
    public OutboundRecord insert(OutboundRecord record) {
        byPackage.put(record.getPackageNo(), record);
        byOutbound.put(record.getOutboundNo(), record);
        orderToPackage.put(record.getSourceOrderNo(), record.getPackageNo());
        return record;
    }

    @Override
    public void updateStatus(String outboundNo, String status) {
        OutboundRecord r = byOutbound.get(outboundNo);
        if (r != null) {
            r.setStatus(status);
            byPackage.put(r.getPackageNo(), r);
            byOutbound.put(outboundNo, r);
        }
    }

    @Override
    public long countByPackageNo(String packageNo) {
        return byPackage.containsKey(packageNo) ? 1 : 0;
    }
}
