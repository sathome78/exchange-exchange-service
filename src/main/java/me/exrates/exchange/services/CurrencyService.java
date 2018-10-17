package me.exrates.exchange.services;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.components.ExchangerFactory;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
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
    public BigDecimal getBTCRateForCurrency(String currencySymbol) {
        final ExchangerType type = currencyRepository.getType(currencySymbol);

        Exchanger exchanger = factory.getExchanger(type);
        BigDecimal btcRate = exchanger.getRate(currencySymbol, BaseCurrency.BTC);
        if (BigDecimal.ZERO.compareTo(btcRate) > 0) {
            log.info("The exchange rate is taken from {} server: BTC {}", type, btcRate);
            currencyRepository.updateBtcRate(currencySymbol, btcRate);

            return btcRate;
        }
        btcRate = currencyRepository.getBtcRate(currencySymbol);

        log.info("The exchange rate is taken from database: BTC {}", btcRate);
        return btcRate;
    }

    @Transactional
    public BigDecimal getUSDRateForCurrency(String currencySymbol) {
        final ExchangerType type = currencyRepository.getType(currencySymbol);

        Exchanger exchanger = factory.getExchanger(type);
        BigDecimal usdRate = exchanger.getRate(currencySymbol, BaseCurrency.USD);
        if (BigDecimal.ZERO.compareTo(usdRate) > 0) {
            log.info("The exchange rate is taken from {} server: USD {}", type, usdRate);
            currencyRepository.updateUsdRate(currencySymbol, usdRate);

            return usdRate;
        }
        usdRate = currencyRepository.getUsdRate(currencySymbol);

        log.info("The exchange rate is taken from database: USD {}", usdRate);
        return usdRate;
    }

    @Transactional
    public void create(CurrencyForm form) {
        Currency entity = Currency.builder()
                .name(form.getSymbol())
                .type(form.getType())
                .btcRate(form.getBtcRate())
                .btcRateUpdatedAt(form.getBtcRateUpdatedAt())
                .usdRate(form.getUsdRate())
                .usdRateUpdatedAt(form.getUsdRateUpdatedAt())
                .build();
        currencyRepository.save(entity);
        log.info("Currency {} has been created", form.getSymbol());
    }

    @Transactional
    public void updateExchangerType(String currencySymbol, ExchangerType newType) {
        currencyRepository.updateExchangerType(currencySymbol, newType);
        log.info("Currency {} exchanger type has been updated: {}", currencySymbol, newType);
    }
}
