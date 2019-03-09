package me.exrates.exchange.models.enums;

import java.util.stream.Stream;

public enum Market {

    USD, BTC, ETH, FIAT, USDT, UNDEFINED;

    public static Market of(String type) {
        return Stream.of(Market.values())
                .filter(market -> market.name().equals(type.toUpperCase()))
                .findFirst()
                .orElse(Market.UNDEFINED);
    }
}