package com.scm.wms.inventory.reserve;

import java.util.Optional;

public interface ReserveRepository {

    Optional<OrderReserve> findByIdempotencyKey(String key);

    Optional<OrderReserve> findByOrderNo(String orderNo);

    void save(OrderReserve reserve);

    void updateStatus(String orderNo, String status);
}
