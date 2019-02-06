package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.converters.LocalDateTimeToMillisecondsConverter;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.Direction;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Lazy
@Component("exratesExchanger")
public class ExratesExchanger implements Exchanger {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String BTC = "bitcoin";
    private static final String ETH = "ethereum";

    private static final double DEFAULT_USD_RATE = 0.000001;

    private String apiUrlHistory;
    private long period;

    private final RestTemplate restTemplate;
    private final Exchanger coinmarketcupExchanger;
    private final Cache<String, Double> usdRates;

    @Autowired
    public ExratesExchanger(@Value("${exchangers.exrates.api-url.trade-history}") String apiUrlHistory,
                            @Value("${exchangers.exrates.period}") long period,
                            @Qualifier("coinMarketCupExchanger") Exchanger coinmarketcupExchanger) {
        this.apiUrlHistory = apiUrlHistory;
        this.period = period;

        this.coinmarketcupExchanger = coinmarketcupExchanger;
        this.restTemplate = new RestTemplate();
        this.usdRates = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.EXRATES;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        Double btcUsd;
        try {
            btcUsd = usdRates.get(BTC, () -> getUsdRateFromCoinmarketcup(BTC));
        } catch (ExecutionException ex) {
            btcUsd = 0d;
        }

        Double ethUsd;
        try {
            ethUsd = usdRates.get(ETH, () -> getUsdRateFromCoinmarketcup(ETH));
        } catch (ExecutionException ex) {
            ethUsd = 0d;
        }

        Map<Long, Pair<BaseCurrency, Double>> data = getHistoryDataByBaseCurrencies(currencySymbol);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Exrates server is not available");
            return CurrencyDto.builder()
                    .symbol(currencySymbol)
                    .exchangerType(getExchangerType())
                    .usdRate(BigDecimal.valueOf(DEFAULT_USD_RATE))
                    .btcRate(BigDecimal.valueOf(DEFAULT_USD_RATE / btcUsd))
                    .build();
        }
        Map.Entry<Long, Pair<BaseCurrency, Double>> entry = Collections.max(data.entrySet(), Map.Entry.comparingByKey());
        Pair<BaseCurrency, Double> value = entry.getValue();

        final BaseCurrency baseCurrency = value.getLeft();

        double usdRate;
        switch (baseCurrency) {
            case USD:
                usdRate = value.getRight();
                break;
            case BTC:
                usdRate = value.getRight() * btcUsd;
                break;
            case ETH:
                usdRate = value.getRight() * ethUsd;
                break;
            default:
                usdRate = 0;
        }

        double btcRate = usdRate / btcUsd;

        return CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .usdRate(usdRate != 0 ? BigDecimal.valueOf(usdRate) : BigDecimal.valueOf(DEFAULT_USD_RATE))
                .btcRate(btcRate != 0 ? BigDecimal.valueOf(btcRate) : BigDecimal.valueOf(DEFAULT_USD_RATE / btcUsd))
                .build();
    }

    private Double getUsdRateFromCoinmarketcup(String symbol) {
        CurrencyDto currencyDto = coinmarketcupExchanger.getRate(symbol);
        if (isNull(currencyDto)) {
            return 0d;
        }
        return currencyDto.getUsdRate().doubleValue();
    }

    private Map<Long, Pair<BaseCurrency, Double>> getHistoryDataByBaseCurrencies(String currencySymbol) {
        return Stream.of(BaseCurrency.values())
                .map(value -> getHistoryDataByBaseCurrency(currencySymbol, value))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private Pair<Long, Pair<BaseCurrency, Double>> getHistoryDataByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        LocalDate now = LocalDate.now();

        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("from_date", now.minusMonths(period).format(FORMATTER));
        requestParameters.add("to_date", now.format(FORMATTER));
        requestParameters.add("limit", "1");
        requestParameters.add("direction", Direction.DESC.name());

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(String.format(apiUrlHistory, currencySymbol.toLowerCase(), baseCurrency.name().toLowerCase()))
                .queryParams(requestParameters)
                .build();

        LocalDateTime dateBefore = LocalDateTime.now().minusMonths(period);

        ResponseEntity<TradeHistoryData> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(builder.toUriString(), TradeHistoryData.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("Exrates server is not available");
            }
        } catch (Exception ex) {
            log.warn("Error {}-{}-{}: {}", getExchangerType(), baseCurrency.name(), currencySymbol, ex.getMessage());
            return Pair.of(LocalDateTimeToMillisecondsConverter.convert(dateBefore), Pair.of(baseCurrency, 0d));
        }
        TradeHistoryData historyData = responseEntity.getBody();
        if (isNull(historyData) || isNull(historyData.body) || historyData.body.isEmpty()) {
            return Pair.of(LocalDateTimeToMillisecondsConverter.convert(dateBefore), Pair.of(baseCurrency, 0d));
        }
        Body body = historyData.body.get(0);

        return Pair.of(body.dateAcceptance, Pair.of(baseCurrency, body.price));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class TradeHistoryData {

        @Valid
        List<Body> body;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class Body {

        double price;
        @JsonProperty("date_acceptance")
        long dateAcceptance;
    }
}