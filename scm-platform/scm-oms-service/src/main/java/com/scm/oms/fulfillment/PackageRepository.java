package com.scm.oms.fulfillment;

import java.util.List;
import java.util.Optional;

public interface PackageRepository {
    void save(PackageRecord pkg);

    Optional<PackageRecord> findByOrderNo(String orderNo);

    List<PackageRecord> findAllByOrderNo(String orderNo);

    Optional<PackageRecord> findByPackageNo(String packageNo);
}
