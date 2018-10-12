package me.exrates.exchange.services;

import me.exrates.exchange.components.ExchangerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyExchangeService {

    private final ExchangerFactory factory;

    @Autowired
    public CurrencyExchangeService(ExchangerFactory factory) {
        this.factory = factory;
    }

    public BigDecimal getBTCRateForCurrency(String currencyName) {
        return null;
    }


    public BigDecimal getBTCRateForAll() {
        return null;
    }

    public BigDecimal getUSDRateForCurrency(String currencyName) {
        return null;
    }

    public BigDecimal getUSDRateForAll() {
        return null;
    }
}
