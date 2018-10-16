CREATE TABLE currency (
  name                  VARCHAR(5)  NOT NULL PRIMARY KEY,
  type                  VARCHAR(25) NOT NULL,
  usd_rate              NUMERIC(19, 6),
  usd_rate_updated_at   TIMESTAMP NULL,
  btc_rate              NUMERIC(19, 6),
  btc_rate_updated_at   TIMESTAMP NULL
);