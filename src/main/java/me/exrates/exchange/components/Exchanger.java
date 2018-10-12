package me.exrates.exchange.components;

import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.ExchangerType;

import java.math.BigDecimal;

public interface Exchanger {

    ExchangerType getExchangerType();

    BigDecimal getBTCRate(String currencyName) throws ExchangerException;

    BigDecimal getUSDRate(String currencyName);
}
