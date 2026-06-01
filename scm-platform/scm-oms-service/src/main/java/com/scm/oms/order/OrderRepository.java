package com.scm.oms.order;

import java.util.Optional;

public interface OrderRepository {
    Optional<OrderRecord> findByClientToken(String buyerId, String clientToken);

    Optional<OrderRecord> findByOrderNo(String orderNo);

    OrderRecord saveNew(OrderRecord order);

    void update(OrderRecord order);

    long countByBuyerAndToken(String buyerId, String clientToken);
}
