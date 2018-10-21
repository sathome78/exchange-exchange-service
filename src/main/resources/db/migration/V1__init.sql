CREATE TABLE IF NOT EXISTS currency (
  symbol                VARCHAR(16)     NOT NULL PRIMARY KEY,
  type                  VARCHAR(32)     NOT NULL,
  usd_rate              NUMERIC(19, 8)  DEFAULT 0,
  usd_rate_updated_at   TIMESTAMP NULL  DEFAULT CURRENT_TIMESTAMP,
  btc_rate              NUMERIC(19, 8)  DEFAULT 0,
  btc_rate_updated_at   TIMESTAMP NULL  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coinlib_dictionary (
  id                      INT(10)       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  currency_symbol         VARCHAR(32)   NOT NULL,
  coinlib_currency_symbol VARCHAR(32)   NOT NULL,
  coinlib_currency_name   VARCHAR(64),
  enabled                 TINYINT(1)
);

CREATE TABLE IF NOT EXISTS coinmarketcup_dictionary (
  id                            INT(10)       NOT NULL PRIMARY KEY AUTO_INCREMENT,
  currency_symbol               VARCHAR(32)   NOT NULL,
  coinmarketcup_currency_symbol VARCHAR(32)   NOT NULL,
  coinmarketcup_currency_name   VARCHAR(64),
  enabled                       TINYINT(1)
);