CREATE TABLE IF NOT EXISTS outbound_order (
    outbound_no      VARCHAR(64)  NOT NULL PRIMARY KEY,
    package_no       VARCHAR(64)  NOT NULL UNIQUE,
    source_order_no  VARCHAR(64)  NOT NULL,
    status           VARCHAR(16)  NOT NULL,
    created_at       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_source_order (source_order_no)
);
