CREATE TABLE IF NOT EXISTS inv_stock_lot (
    lot_id         VARCHAR(64)  NOT NULL PRIMARY KEY,
    warehouse_id   VARCHAR(32)  NOT NULL,
    sku_id         VARCHAR(32)  NOT NULL,
    qty_available  INT          NOT NULL,
    expire_date    DATE         NOT NULL,
    KEY idx_wh_sku_expire (warehouse_id, sku_id, expire_date)
);

CREATE TABLE IF NOT EXISTS inv_order_reserve (
    order_no         VARCHAR(64)  NOT NULL PRIMARY KEY,
    idempotency_key  VARCHAR(128) NOT NULL UNIQUE,
    status           VARCHAR(16)  NOT NULL,
    created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE TABLE IF NOT EXISTS inv_reserve_line (
    order_no   VARCHAR(64) NOT NULL,
    lot_id     VARCHAR(64) NOT NULL,
    qty        INT         NOT NULL,
    PRIMARY KEY (order_no, lot_id),
    KEY idx_lot (lot_id)
);
