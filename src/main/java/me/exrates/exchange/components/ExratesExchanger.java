package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component("exratesExchanger")
public class ExratesExchanger implements Exchanger {

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.EXRATES;
    }

    @Override
    public BigDecimal getBTCRate(String currencyName) {
        return null;
    }

    @Override
    public BigDecimal getUSDRate(String currencyName) {
        return null;
    }
}
