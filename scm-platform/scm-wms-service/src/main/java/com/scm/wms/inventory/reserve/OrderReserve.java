package com.scm.wms.inventory.reserve;

import com.scm.wms.inventory.fefo.AllocationLine;

import java.util.List;

public record OrderReserve(
        String orderNo,
        String idempotencyKey,
        String status,
        List<AllocationLine> lines) {
}
