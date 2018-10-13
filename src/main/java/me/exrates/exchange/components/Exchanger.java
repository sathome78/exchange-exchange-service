package me.exrates.exchange.components;

import me.exrates.exchange.models.enums.ExchangerType;

import java.math.BigDecimal;

public interface Exchanger {

    ExchangerType getExchangerType();

    BigDecimal getBTCRate(String currencyName);

    BigDecimal getUSDRate(String currencyName);
}
