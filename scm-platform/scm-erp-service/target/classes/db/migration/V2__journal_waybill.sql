ALTER TABLE journal_entry
    ADD COLUMN waybill_no VARCHAR(64) NULL AFTER status,
    ADD KEY idx_waybill (waybill_no);
