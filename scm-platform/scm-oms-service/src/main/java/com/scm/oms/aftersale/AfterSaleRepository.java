package com.scm.oms.aftersale;

import java.util.Optional;

public interface AfterSaleRepository {
    AfterSaleRecord save(AfterSaleRecord record);

    Optional<AfterSaleRecord> findByOrderNo(String orderNo);

    Optional<AfterSaleRecord> findByAfterSaleNo(String afterSaleNo);

    void update(AfterSaleRecord record);
}
