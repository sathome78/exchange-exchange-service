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
import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

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
        if (btcRate.compareTo(BigDecimal.ZERO) > 0) {
            log.info("The exchange rate is taken from {} server: BTC {}", type, btcRate);
            currencyRepository.updateBtcRate(currencySymbol, btcRate);

            return btcRate;
        }
        btcRate = currencyRepository.getBtcRate(currencySymbol);

        log.info("The exchange rate is taken from database: BTC {}", btcRate);
        return nonNull(btcRate) ? btcRate : BigDecimal.ZERO;
    }

    @Transactional
    public BigDecimal getUSDRateForCurrency(String currencySymbol) {
        final ExchangerType type = currencyRepository.getType(currencySymbol);

        Exchanger exchanger = factory.getExchanger(type);
        BigDecimal usdRate = exchanger.getRate(currencySymbol, BaseCurrency.USD);
        if (usdRate.compareTo(BigDecimal.ZERO) > 0) {
            log.info("The exchange rate is taken from {} server: USD {}", type, usdRate);
            currencyRepository.updateUsdRate(currencySymbol, usdRate);

            return usdRate;
        }
        usdRate = currencyRepository.getUsdRate(currencySymbol);

        log.info("The exchange rate is taken from database: USD {}", usdRate);
        return nonNull(usdRate) ? usdRate : BigDecimal.ZERO;
    }

    @Transactional
    public Currency create(CurrencyForm form) {
        final LocalDateTime now = LocalDateTime.now();

        Currency entity = Currency.builder()
                .name(form.getName())
                .type(form.getType())
                .btcRate(form.getBtcRate())
                .btcRateUpdatedAt(now)
                .usdRate(form.getUsdRate())
                .usdRateUpdatedAt(now)
                .build();
        Currency newCurrency = currencyRepository.save(entity);
        log.info("Currency {} has been created", form.getName());
        return newCurrency;
    }

    @Transactional
    public void delete(String currencySymbol) {
        currencyRepository.deleteById(currencySymbol);
        log.info("Currency {} has been removed", currencySymbol);
    }

    @Transactional
    public void updateExchangerType(String currencySymbol, ExchangerType newType) {
        currencyRepository.updateExchangerType(currencySymbol, newType);
        log.info("Currency {} exchanger type has been updated: {}", currencySymbol, newType);
    }
}
