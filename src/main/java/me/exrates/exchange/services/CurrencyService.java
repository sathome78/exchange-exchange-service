package me.exrates.exchange.services;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.components.ExchangerFactory;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.CurrencyType;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.repositories.CurrencyRepository;
import me.exrates.exchange.utils.ExecutorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

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

    @Transactional(readOnly = true)
    public Currency getRatesByCurrencySymbol(String currencySymbol) {
        Currency currency = currencyRepository.getBySymbol(currencySymbol);
        if (isNull(currency)) {
            log.info("Currency {} is not present in database", currencySymbol);
            return null;
        }
        log.info("The exchange rate for {} is taken from database: BTC {}, USD {}", currencySymbol, currency.getBtcRate(), currency.getUsdRate());
        return currency;
    }

    @Transactional(readOnly = true)
    public List<Currency> getRatesForAll() {
        List<Currency> all = currencyRepository.findAll();
        if (isEmpty(all)) {
            log.info("No currencies are present in database");
            return Collections.emptyList();
        }
        return all;
    }

    @Transactional(readOnly = true)
    public List<Currency> getRatesByCurrencyType(String type) {
        Stream<Currency> currencyStream = currencyRepository.findAll().stream();

        final CurrencyType currencyType = CurrencyType.of(type);

        switch (currencyType) {
            case CRYPTO:
                return currencyStream
                        .filter(currency -> !Objects.equals(ExchangerType.FREE_CURRENCY, currency.getExchangerType()))
                        .collect(toList());
            case FIAT:
                return currencyStream
                        .filter(currency -> Objects.equals(ExchangerType.FREE_CURRENCY, currency.getExchangerType()))
                        .collect(toList());
            default:
                return Collections.emptyList();
        }
    }

    @Transactional
    public Currency create(CurrencyForm form) {
        final LocalDateTime now = LocalDateTime.now();

        Currency newCurrency = Currency.builder()
                .symbol(form.getSymbol())
                .exchangerType(form.getExchangerType())
                .exchangerSymbol(form.getExchangerSymbol())
                .usdRate(form.getUsdRate())
                .usdRateUpdatedAt(now)
                .btcRate(form.getBtcRate())
                .btcRateUpdatedAt(now)
                .build();
        currencyRepository.save(newCurrency);
        log.info("Currency {} has been created", form.getSymbol());
        return newCurrency;
    }

    @Transactional
    public void delete(String currencySymbol) {
        currencyRepository.deleteById(currencySymbol);
        log.info("Currency {} has been removed", currencySymbol);
    }

    @Transactional
    public void updateCurrency(String currencySymbol, ExchangerType exchangerType, String exchangerSymbol) {
        currencyRepository.updateCurrency(currencySymbol, exchangerType, exchangerSymbol);
        log.info("Currency {} exchanger type has been updated: {}", currencySymbol, exchangerType);
    }

    @Transactional
    public void refreshCurrencyRate() {
        List<Currency> all = currencyRepository.findAll();
        if (isEmpty(all)) {
            log.info("No currencies present in database");
            return;
        }
        Map<ExchangerType, List<Currency>> groupedByType = all.stream().collect(Collectors.groupingBy(Currency::getExchangerType));

        ExecutorService executor = Executors.newFixedThreadPool(groupedByType.size());

        groupedByType.forEach((key, value) -> CompletableFuture.runAsync(() -> refreshRatesByType(key, value), executor));

        ExecutorUtil.shutdownExecutor(executor);
    }

    private void refreshRatesByType(ExchangerType exchangerType, List<Currency> currencies) {
        currencies.forEach(currency -> {
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException ex) {
                log.debug("Delay interrupted!", ex);
            }
            refreshRateByCurrency(exchangerType, currency);
        });
    }

    @Transactional
    public void refreshRateByCurrency(ExchangerType exchangerType, Currency currency) {
        Exchanger exchanger = factory.getExchanger(exchangerType);

        CurrencyDto currencyDto = exchanger.getRate(currency.getExchangerSymbol());
        if (isNull(currencyDto)) {
            log.info("The exchange rate for {} is not taken from {} server", currency.getSymbol(), exchangerType);
            return;
        }
        final BigDecimal btcRate = currencyDto.getBtcRate();
        final BigDecimal usdRate = currencyDto.getUsdRate();
        log.info("The exchange rate for {} is taken from {} server: BTC {}, USD {}", currency.getSymbol(), exchangerType, btcRate, usdRate);
        currencyRepository.updateRates(currency.getSymbol(), btcRate, usdRate);
    }
}
