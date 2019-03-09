package me.exrates.exchange.services;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.cache.ExchangeRatesRedisRepository;
import me.exrates.exchange.components.exchangers.ExratesExchanger;
import me.exrates.exchange.components.exchangers.FreeCurrencyExchanger;
import me.exrates.exchange.models.dto.CacheOrderStatisticDto;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.enums.Market;
import me.exrates.exchange.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class CurrencyPairService {

    private static final String DELIMITER = "/";

    private static final String BTC_USD = "BTC/USD";
    private static final String ETH_USD = "ETH/USD";

    private final ExratesExchanger exratesExchanger;
    private final FreeCurrencyExchanger freeCurrencyExchanger;
    private final ExchangeRatesRedisRepository redisRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyPairService(ExratesExchanger exratesExchanger,
                               FreeCurrencyExchanger freeCurrencyExchanger,
                               ExchangeRatesRedisRepository redisRepository,
                               CurrencyRepository currencyRepository) {
        this.exratesExchanger = exratesExchanger;
        this.freeCurrencyExchanger = freeCurrencyExchanger;
        this.redisRepository = redisRepository;
        this.currencyRepository = currencyRepository;
    }

    public void refreshAllCurrencyPairRates() {
        List<CacheOrderStatisticDto> statisticList = initExratesList();

        redisRepository.batchUpdate(statisticList);

        List<CacheOrderStatisticDto> statisticFiatList = initFiatList();

        redisRepository.batchUpdate(statisticFiatList);
    }

    private List<CacheOrderStatisticDto> initExratesList() {
        List<CacheOrderStatisticDto> statisticList = exratesExchanger.getAllStatisticFromMarket();

        statisticList.forEach(statistic -> statistic.setPriceInUSD(calculatePriceInUSD(statistic)));

        return statisticList;
    }

    private List<CacheOrderStatisticDto> initFiatList() {
        return currencyRepository.findAll().stream()
                .filter(currency -> Objects.equals(ExchangerType.FREE_CURRENCY, currency.getExchangerType()))
                .filter(currency -> !BaseCurrency.USD.name().equals(currency.getSymbol()))
                .map(currency -> {
                    final String pairName = String.join(DELIMITER, currency.getSymbol(), BaseCurrency.USD.name());
                    final BigDecimal usdRate = currency.getUsdRate();

                    CacheOrderStatisticDto statistic = CacheOrderStatisticDto.builder()
                            .currencyPairName(pairName)
                            .lastOrderRate(usdRate)
                            .predLastOrderRate(usdRate)
                            .percentChange(BigDecimal.ZERO)
                            .currencyVolume(BigDecimal.ZERO)
                            .volume(BigDecimal.ZERO)
                            .market(Market.FIAT.name())
                            .build();

                    return statistic.toBuilder()
                            .priceInUSD(calculatePriceInUSD(statistic))
                            .build();
                })
                .collect(toList());
    }

    public void refreshCurrencyPairRate(CacheOrderStatisticDto statistic) {
        CacheOrderStatisticDto cachedStatistic = redisRepository.get(statistic.getCurrencyPairName());
        if (isNull(cachedStatistic)) {
            redisRepository.put(statistic.toBuilder()
                    .priceInUSD(calculatePriceInUSD(statistic))
                    .build());
        } else {
            final BigDecimal predLastOrderRate = cachedStatistic.getLastOrderRate();
            final BigDecimal percentChange = statistic.getLastOrderRate()
                    .divide(predLastOrderRate, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .subtract(BigDecimal.valueOf(100));
            final BigDecimal volume = cachedStatistic.getVolume();
            final BigDecimal currencyVolume = cachedStatistic.getCurrencyVolume();

            redisRepository.update(cachedStatistic.toBuilder()
                    .lastOrderRate(statistic.getLastOrderRate())
                    .predLastOrderRate(predLastOrderRate)
                    .percentChange(percentChange)
                    .volume(statistic.getVolume().add(volume))
                    .currencyVolume(statistic.getCurrencyVolume().add(currencyVolume))
                    .priceInUSD(calculatePriceInUSD(statistic))
                    .build());
        }
    }

    private BigDecimal calculatePriceInUSD(CacheOrderStatisticDto statistic) {
        final String market = statistic.getMarket();
        final BigDecimal lastOrderRate = statistic.getLastOrderRate();

        switch (Market.of(market)) {
            case USD:
            case USDT:
                return lastOrderRate;
            case BTC:
                CacheOrderStatisticDto btcStatistic = exratesExchanger.getStatisticFromMarketByCurrencyPair(BTC_USD);
                BigDecimal btcLastOrderRate = nonNull(btcStatistic) ? btcStatistic.getLastOrderRate() : BigDecimal.ZERO;
                return btcLastOrderRate.multiply(lastOrderRate);
            case ETH:
                CacheOrderStatisticDto ethStatistic = exratesExchanger.getStatisticFromMarketByCurrencyPair(ETH_USD);
                BigDecimal ethLastOrderRate = nonNull(ethStatistic) ? ethStatistic.getLastOrderRate() : BigDecimal.ZERO;
                return ethLastOrderRate.multiply(lastOrderRate);
            case FIAT:
                CurrencyDto rate = freeCurrencyExchanger.getRate(statistic.getCurrencyPairName().split(DELIMITER)[1]);
                BigDecimal newLastOrderRate = nonNull(rate) ? rate.getUsdRate() : BigDecimal.ZERO;
                return newLastOrderRate.multiply(lastOrderRate);
            case UNDEFINED:
                return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }
}