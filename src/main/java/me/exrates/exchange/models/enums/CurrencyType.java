package me.exrates.exchange.models.enums;

import java.util.stream.Stream;

public enum CurrencyType {

    CRYPTO, FIAT, UNDEFINED;

    public static CurrencyType of(String type) {
        return Stream.of(CurrencyType.values())
                .filter(currencyType -> currencyType.name().equals(type.toUpperCase()))
                .findFirst()
                .orElse(CurrencyType.UNDEFINED);
    }
}