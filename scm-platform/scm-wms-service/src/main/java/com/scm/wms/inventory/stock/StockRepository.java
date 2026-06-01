package com.scm.wms.inventory.stock;

import com.scm.wms.inventory.fefo.AllocationLine;
import com.scm.wms.inventory.fefo.StockLot;

import java.util.List;

public interface StockRepository {

    List<StockLot> findAvailableLots(String warehouseId, String skuId);

    /** 原子：FEFO 选取并扣减可用量 */
    List<AllocationLine> reserveSku(String warehouseId, String skuId, int qty);

    void applyDeductions(List<AllocationLine> lines);

    void restore(List<AllocationLine> lines);

    void seedDemoLotsIfEmpty();
}
