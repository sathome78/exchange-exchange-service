CREATE TABLE IF NOT EXISTS currency (
  symbol                VARCHAR(16)       NOT NULL PRIMARY KEY,
  exchanger_type        VARCHAR(32)       NOT NULL,
  exchanger_symbol      VARCHAR(32)       NOT NULL,
  usd_rate              NUMERIC(19, 12)   DEFAULT 0,
  usd_rate_updated_at   TIMESTAMP         NULL DEFAULT CURRENT_TIMESTAMP,
  btc_rate              NUMERIC(19, 12)   DEFAULT 0,
  btc_rate_updated_at   TIMESTAMP         NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS currency_history (
  id                    BIGINT            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  currency_id           VARCHAR(16)       NOT NULL REFERENCES currency(symbol),
  usd_rate              NUMERIC(19, 12)   DEFAULT 0,
  btc_rate              NUMERIC(19, 12)   DEFAULT 0,
  created_at            DATE              NOT NULL
);