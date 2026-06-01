package com.scm.oms.fulfillment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "memory", matchIfMissing = true)
public class InMemoryPackageRepository implements PackageRepository {
    private final Map<String, PackageRecord> byPackageNo = new ConcurrentHashMap<>();

    @Override
    public void save(PackageRecord pkg) {
        byPackageNo.put(pkg.getPackageNo(), pkg);
    }

    @Override
    public Optional<PackageRecord> findByOrderNo(String orderNo) {
        return byPackageNo.values().stream()
                .filter(p -> orderNo.equals(p.getOrderNo()))
                .findFirst();
    }

    @Override
    public List<PackageRecord> findAllByOrderNo(String orderNo) {
        return byPackageNo.values().stream()
                .filter(p -> orderNo.equals(p.getOrderNo()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PackageRecord> findByPackageNo(String packageNo) {
        return Optional.ofNullable(byPackageNo.get(packageNo));
    }
}
