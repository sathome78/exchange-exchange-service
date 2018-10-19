package me.exrates.exchange.components;

import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;

public interface Exchanger {

    ExchangerType getExchangerType();

    CurrencyDto getRate(String currencyName);
}
