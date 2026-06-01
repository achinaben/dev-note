CREATE TABLE IF NOT EXISTS trade_order (
    order_no        VARCHAR(32)  NOT NULL PRIMARY KEY,
    trade_no        VARCHAR(32)  NOT NULL,
    buyer_id        VARCHAR(64)  NOT NULL,
    client_token    VARCHAR(128) NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    pay_amount      DECIMAL(18,4) NOT NULL,
    pay_time        DATETIME(3)  NULL,
    version         INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_buyer_client (buyer_id, client_token)
);

CREATE TABLE IF NOT EXISTS oms_package (
    package_no   VARCHAR(64) NOT NULL PRIMARY KEY,
    order_no     VARCHAR(32) NOT NULL,
    outbound_no  VARCHAR(64) NULL,
    status       VARCHAR(32) NOT NULL,
    KEY idx_order (order_no)
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_type   VARCHAR(64)  NOT NULL,
    biz_key      VARCHAR(128) NOT NULL UNIQUE,
    occurred_at  DATETIME(3)  NOT NULL,
    payload_json TEXT         NOT NULL,
    published    TINYINT(1)   NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_payment (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(32)  NOT NULL,
    notify_id   VARCHAR(128) NOT NULL,
    status      VARCHAR(16)  NOT NULL,
    UNIQUE KEY uk_notify (notify_id)
);

CREATE TABLE IF NOT EXISTS after_sale (
    after_sale_no VARCHAR(32) NOT NULL PRIMARY KEY,
    order_no      VARCHAR(32) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    refund_amount DECIMAL(18,4) NOT NULL,
    created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);
