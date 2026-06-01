CREATE TABLE IF NOT EXISTS tms_shipment (
    shipment_no   VARCHAR(64)  NOT NULL PRIMARY KEY,
    package_no    VARCHAR(64)  NOT NULL UNIQUE,
    order_no      VARCHAR(64)  NULL,
    waybill_no    VARCHAR(64)  NOT NULL,
    carrier_code  VARCHAR(16)  NOT NULL,
    label_url     VARCHAR(256) NULL,
    status        VARCHAR(16)  NOT NULL,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_order (order_no)
);
