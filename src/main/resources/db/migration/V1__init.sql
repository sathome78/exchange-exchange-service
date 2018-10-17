CREATE TABLE currency (
  name                  VARCHAR(16)  NOT NULL PRIMARY KEY,
  type                  VARCHAR(32) NOT NULL,
  usd_rate              NUMERIC(19, 8) DEFAULT 0,
  usd_rate_updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  btc_rate              NUMERIC(19, 8) DEFAULT 0,
  btc_rate_updated_at   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);