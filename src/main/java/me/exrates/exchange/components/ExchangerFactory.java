package me.exrates.exchange.components;

import me.exrates.exchange.models.enums.ExchangerType;

import java.util.Set;

public interface ExchangerFactory {

    Set<ExchangerType> getAvailableExchangers();

    Exchanger getExchanger(ExchangerType exchangerType);
}
