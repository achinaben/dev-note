package com.scm.wms.inventory.fefo;

import java.time.LocalDate;

public record StockLot(String lotId, String warehouseId, String skuId, int qtyAvailable, LocalDate expireDate) {
}
