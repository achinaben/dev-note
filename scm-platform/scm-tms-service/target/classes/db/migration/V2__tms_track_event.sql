CREATE TABLE IF NOT EXISTS tms_track_event (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_no   VARCHAR(64)  NOT NULL,
    event_code   VARCHAR(32)  NOT NULL,
    source       VARCHAR(16)  NOT NULL,
    event_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_waybill (waybill_no, event_at)
);
