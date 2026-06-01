package com.scm.wms.pick;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class MemoryPickConfirmStore implements PickConfirmStore {

    private final Map<String, String> outboundByOperation = new ConcurrentHashMap<>();

    @Override
    public boolean exists(String operationId) {
        return outboundByOperation.containsKey(operationId);
    }

    @Override
    public void save(String operationId, String outboundNo) {
        outboundByOperation.put(operationId, outboundNo);
    }

    @Override
    public Optional<String> findOutboundNo(String operationId) {
        return Optional.ofNullable(outboundByOperation.get(operationId));
    }
}
