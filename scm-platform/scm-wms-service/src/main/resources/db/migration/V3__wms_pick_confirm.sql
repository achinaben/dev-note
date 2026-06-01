CREATE TABLE IF NOT EXISTS wms_pick_confirm (
    operation_id  VARCHAR(128) NOT NULL PRIMARY KEY,
    outbound_no   VARCHAR(64)  NOT NULL,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_outbound (outbound_no)
);
