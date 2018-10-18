package me.exrates.exchange.services;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.components.ExchangerFactory;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.dto.RateDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.repositories.CurrencyRepository;
import me.exrates.exchange.utils.ExecutorUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

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
            log.info("The exchange rate for {} is taken from {} server: BTC {}", currencySymbol, type, btcRate);
            currencyRepository.updateBtcRate(currencySymbol, btcRate);

            return btcRate;
        }
        btcRate = currencyRepository.getBtcRate(currencySymbol);

        log.info("The exchange rate for {} is taken from database: BTC {}", currencySymbol, btcRate);
        return nonNull(btcRate) ? btcRate : BigDecimal.ZERO;
    }

    @Transactional
    public Map<ExchangerType, List<RateDto>> getBTCRateForAll() {
        List<Currency> all = currencyRepository.findAll();
        if (isEmpty(all)) {
            log.info("No currencies present in database");
            return Collections.emptyMap();
        }
        Map<ExchangerType, List<Currency>> groupedByType = all.stream().collect(Collectors.groupingBy(Currency::getType));

        ExecutorService executor = Executors.newFixedThreadPool(groupedByType.size());

        List<CompletableFuture<Pair<ExchangerType, List<RateDto>>>> future = groupedByType.entrySet().stream()
                .map(entry ->
                        CompletableFuture.supplyAsync(() -> Pair.of(entry.getKey(), getBTCRateByType(entry.getValue())), executor)
                                .exceptionally(ex -> {
                                    log.error("Get BTC rates failed ({})", entry.getKey(), ex);
                                    return Pair.of(entry.getKey(), Collections.emptyList());
                                }))
                .collect(toList());

        Map<ExchangerType, List<RateDto>> collect = future.stream()
                .map(CompletableFuture::join)
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));

        ExecutorUtil.shutdownExecutor(executor);

        return collect;
    }

    private List<RateDto> getBTCRateByType(List<Currency> currencies) {
        return currencies.stream()
                .map(Currency::getName)
                .map(currencySymbol -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(2500);
                    } catch (InterruptedException ex) {
                        log.debug("Delay interrupted!");
                    }
                    return new RateDto(currencySymbol, getBTCRateForCurrency(currencySymbol));
                })
                .collect(toList());
    }

    @Transactional
    public BigDecimal getUSDRateForCurrency(String currencySymbol) {
        final ExchangerType type = currencyRepository.getType(currencySymbol);

        Exchanger exchanger = factory.getExchanger(type);
        BigDecimal usdRate = exchanger.getRate(currencySymbol, BaseCurrency.USD);
        if (usdRate.compareTo(BigDecimal.ZERO) > 0) {
            log.info("The exchange rate for {} is taken from {} server: USD {}", currencySymbol, type, usdRate);
            currencyRepository.updateUsdRate(currencySymbol, usdRate);

            return usdRate;
        }
        usdRate = currencyRepository.getUsdRate(currencySymbol);

        log.info("The exchange rate for {} is taken from database: USD {}", currencySymbol, usdRate);
        return nonNull(usdRate) ? usdRate : BigDecimal.ZERO;
    }

    @Transactional
    public Map<ExchangerType, List<RateDto>> getUSDRateForAll() {
        List<Currency> all = currencyRepository.findAll();
        if (isEmpty(all)) {
            log.info("No currencies present in database");
            return Collections.emptyMap();
        }
        Map<ExchangerType, List<Currency>> groupedByType = all.stream().collect(Collectors.groupingBy(Currency::getType));

        ExecutorService executor = Executors.newFixedThreadPool(groupedByType.size());

        List<CompletableFuture<Pair<ExchangerType, List<RateDto>>>> future = groupedByType.entrySet().stream()
                .map(entry ->
                        CompletableFuture.supplyAsync(() -> Pair.of(entry.getKey(), getUSDRateByType(entry.getValue())), executor)
                                .exceptionally(ex -> {
                                    log.error("Get USD rates failed ({})", entry.getKey(), ex);
                                    return Pair.of(entry.getKey(), Collections.emptyList());
                                }))
                .collect(toList());

        Map<ExchangerType, List<RateDto>> collect = future.stream()
                .map(CompletableFuture::join)
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));

        ExecutorUtil.shutdownExecutor(executor);

        return collect;
    }

    private List<RateDto> getUSDRateByType(List<Currency> currencies) {
        return currencies.stream()
                .map(Currency::getName)
                .map(currencySymbol -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(2500);
                    } catch (InterruptedException ex) {
                        log.debug("Delay interrupted!");
                    }
                    return new RateDto(currencySymbol, getUSDRateForCurrency(currencySymbol));
                })
                .collect(toList());
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
