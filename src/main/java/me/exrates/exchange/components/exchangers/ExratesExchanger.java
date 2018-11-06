package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

@Slf4j
@Lazy
@DependsOn("coinMarketCupExchanger")
@Component("exratesExchanger")
public class ExratesExchanger implements Exchanger {

    private static final String BTC = "bitcoin";
    private static final String ETH = "ethereum";

    private static final double DEFAULT_USD_RATE = 0.000001;

    private String apiUrlTicker;

    private final RestTemplate restTemplate;
    private final Exchanger coinmarketcupExchanger;
    private final Cache<String, Double> usdRates;

    @Autowired
    public ExratesExchanger(@Value("${exchangers.exrates.api-url.ticker}") String apiUrlTicker,
                            @Qualifier("coinMarketCupExchanger") Exchanger coinmarketcupExchanger) {
        this.apiUrlTicker = apiUrlTicker;
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

        Map<BaseCurrency, List<ExratesData>> data = getDataFromMarket(currencySymbol);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Exrates server is not available");
            return CurrencyDto.builder()
                    .symbol(currencySymbol)
                    .exchangerType(getExchangerType())
                    .usdRate(BigDecimal.valueOf(DEFAULT_USD_RATE))
                    .btcRate(BigDecimal.valueOf(DEFAULT_USD_RATE / btcUsd))
                    .build();
        }

        List<ExratesData> usdData = data.get(BaseCurrency.USD);
        List<ExratesData> btcData = data.get(BaseCurrency.BTC);
        List<ExratesData> ethData = data.get(BaseCurrency.ETH);

        int number = 0;
        double usdRate1;
        if (isNotEmpty(usdData) && nonNull(usdData.get(0))) {
            usdRate1 = usdData.get(0).last;
            number++;
        } else {
            usdRate1 = 0;
        }
        double usdRate2;
        if (isNotEmpty(btcData) && nonNull(btcData.get(0))) {
            usdRate2 = btcData.get(0).last * btcUsd;
            number++;
        } else {
            usdRate2 = 0;
        }
        double usdRate3;
        if (isNotEmpty(ethData) && nonNull(ethData.get(0))) {
            usdRate3 = ethData.get(0).last * ethUsd;
            number++;
        } else {
            usdRate3 = 0;
        }


        final double midUsdRate = (usdRate1 + usdRate2 + usdRate3) / number;
        final double midBtcRate = midUsdRate / btcUsd;

        return CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .usdRate(midUsdRate != 0 ? BigDecimal.valueOf(midUsdRate) : BigDecimal.valueOf(DEFAULT_USD_RATE))
                .btcRate(midBtcRate != 0 ? BigDecimal.valueOf(midBtcRate) : BigDecimal.valueOf(DEFAULT_USD_RATE / btcUsd))
                .build();
    }

    private Double getUsdRateFromCoinmarketcup(String symbol) {
        CurrencyDto currencyDto = coinmarketcupExchanger.getRate(symbol);
        if (isNull(currencyDto)) {
            return 0d;
        }
        return currencyDto.getUsdRate().doubleValue();
    }

    private Map<BaseCurrency, List<ExratesData>> getDataFromMarket(String currencySymbol) {
        return Stream.of(BaseCurrency.values())
                .map(value -> Pair.of(value, getDataFromMarketByBaseCurrency(currencySymbol, value)))
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private List<ExratesData> getDataFromMarketByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("currency_pair", String.format("%s_%s", currencySymbol.toLowerCase(), baseCurrency.name().toLowerCase()));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlTicker)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<ExratesData[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(builder.toUriString(), ExratesData[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("Exrates server is not available");
            }
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        ExratesData[] body = responseEntity.getBody();

        return nonNull(body) ? Stream.of(body).collect(toList()) : Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class ExratesData {

        double last;
    }
}
