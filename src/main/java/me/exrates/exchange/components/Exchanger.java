package me.exrates.exchange.components;

import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;

import java.math.BigDecimal;

public interface Exchanger {

    ExchangerType getExchangerType();

    BigDecimal getRate(String currencyName, BaseCurrency currency);
}
