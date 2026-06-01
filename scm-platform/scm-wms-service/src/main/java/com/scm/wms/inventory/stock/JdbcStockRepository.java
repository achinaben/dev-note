package com.scm.wms.inventory.stock;

import com.scm.wms.inventory.fefo.AllocationLine;
import com.scm.wms.inventory.fefo.FefoAllocator;
import com.scm.wms.inventory.fefo.StockLot;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@ConditionalOnProperty(name = "scm.storage", havingValue = "jdbc")
public class JdbcStockRepository implements StockRepository {

    private final JdbcTemplate jdbc;

    public JdbcStockRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<StockLot> findAvailableLots(String warehouseId, String skuId) {
        return jdbc.query(
                """
                        SELECT lot_id, warehouse_id, sku_id, qty_available, expire_date
                        FROM inv_stock_lot
                        WHERE warehouse_id = ? AND sku_id = ? AND qty_available > 0
                        ORDER BY expire_date, lot_id
                        FOR UPDATE
                        """,
                this::map, warehouseId, skuId);
    }

    @Override
    @Transactional
    public List<AllocationLine> reserveSku(String warehouseId, String skuId, int qty) {
        List<AllocationLine> lines = FefoAllocator.allocate(findAvailableLots(warehouseId, skuId), qty);
        applyDeductions(lines);
        return lines;
    }

    @Override
    public void applyDeductions(List<AllocationLine> lines) {
        for (AllocationLine line : lines) {
            int updated = jdbc.update(
                    "UPDATE inv_stock_lot SET qty_available = qty_available - ? WHERE lot_id = ? AND qty_available >= ?",
                    line.qty(), line.lotId(), line.qty());
            if (updated != 1) {
                throw new IllegalStateException("jdbc 扣减失败 lot=" + line.lotId());
            }
        }
    }

    @Override
    public void restore(List<AllocationLine> lines) {
        for (AllocationLine line : lines) {
            jdbc.update(
                    "UPDATE inv_stock_lot SET qty_available = qty_available + ? WHERE lot_id = ?",
                    line.qty(), line.lotId());
        }
    }

    @Override
    public void seedDemoLotsIfEmpty() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM inv_stock_lot", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        insertLot("LOT-A", "WH-SH-01", "SKU001", 3, LocalDate.of(2026, 6, 1));
        insertLot("LOT-B", "WH-SH-01", "SKU001", 5, LocalDate.of(2026, 7, 1));
        insertLot("LOT-C", "WH-SH-01", "SKU001", 10, LocalDate.of(2026, 8, 1));
    }

    private void insertLot(String lotId, String wh, String sku, int qty, LocalDate expire) {
        jdbc.update(
                "INSERT INTO inv_stock_lot(lot_id, warehouse_id, sku_id, qty_available, expire_date) VALUES(?,?,?,?,?)",
                lotId, wh, sku, qty, expire);
    }

    private StockLot map(ResultSet rs, int row) throws SQLException {
        return new StockLot(
                rs.getString("lot_id"),
                rs.getString("warehouse_id"),
                rs.getString("sku_id"),
                rs.getInt("qty_available"),
                rs.getDate("expire_date").toLocalDate());
    }
}
