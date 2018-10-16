package me.exrates.exchange.services;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.components.ExchangerFactory;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class CurrencyService {

    private final ExchangerFactory factory;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyService(ExchangerFactory factory,
                           CurrencyRepository currencyRepository) {
        this.factory = factory;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public BigDecimal getBTCRateForCurrency(String currencyName) {
        final ExchangerType type = currencyRepository.getType(currencyName);

        Exchanger exchanger = factory.getExchanger(type);
        BigDecimal btcRate = exchanger.getRate(currencyName, BaseCurrency.BTC);
        if (BigDecimal.ZERO.compareTo(btcRate) > 0) {
            log.info("The exchange rate is taken from {}: {}", type, btcRate);
            currencyRepository.updateBtcRate(currencyName, btcRate);

            return btcRate;
        }
        btcRate = currencyRepository.getBtcRate(currencyName);

        log.info("The exchange rate is taken from database: {}", btcRate);
        return btcRate;
    }

    @Transactional
    public BigDecimal getUSDRateForCurrency(String currencyName) {
        final ExchangerType type = currencyRepository.getType(currencyName);

        Exchanger exchanger = factory.getExchanger(type);
        BigDecimal usdRate = exchanger.getRate(currencyName, BaseCurrency.USD);
        if (BigDecimal.ZERO.compareTo(usdRate) > 0) {
            log.info("The exchange rate is taken from {}: {}", type, usdRate);
            currencyRepository.updateUsdRate(currencyName, usdRate);

            return usdRate;
        }
        usdRate = currencyRepository.getUsdRate(currencyName);

        log.info("The exchange rate is taken from database: {}", usdRate);
        return usdRate;
    }

    @Transactional
    public void saveNewCurrency(CurrencyDto currency) {
        Currency entity = Currency.builder()
                .name(currency.getName())
                .type(currency.getType())
                .btcRate(currency.getBtcRate())
                .btcRateUpdatedAt(currency.getBtcRateUpdatedAt())
                .usdRate(currency.getUsdRate())
                .usdRateUpdatedAt(currency.getUsdRateUpdatedAt())
                .build();
        currencyRepository.save(entity);
    }
}
