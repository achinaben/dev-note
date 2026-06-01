CREATE TABLE IF NOT EXISTS journal_entry (

    je_id      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,

    je_no      VARCHAR(32)  NOT NULL UNIQUE,

    biz_key    VARCHAR(128) NOT NULL UNIQUE,

    status     VARCHAR(16)  NOT NULL,

    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3)

);



CREATE TABLE IF NOT EXISTS processed_message (

    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,

    biz_key         VARCHAR(128) NOT NULL,

    consumer_group  VARCHAR(64)  NOT NULL,

    event_id        VARCHAR(64)  NULL,

    processed_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE KEY uk_consumer_biz (consumer_group, biz_key)

);

