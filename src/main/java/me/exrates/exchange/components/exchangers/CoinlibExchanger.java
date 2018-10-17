package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.support.SupportedCoinlibService;
import me.exrates.exchange.utils.ExecutorUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_COINLIB_EXCHANGER;

@Slf4j
@Lazy
@Component("coinlibExchanger")
public class CoinlibExchanger implements Exchanger {

    private String apiUrlCoin;
    private String apiKey;

    private final SupportedCoinlibService supportedService;
    private final Cache cache;
    private final RestTemplate restTemplate;

    public CoinlibExchanger(@Value("${exchangers.coinlib.api-url.coin}") String apiUrlCoin,
                            @Value("${exchangers.coinlib.api-key}") String apiKey,
                            SupportedCoinlibService supportedService,
                            @Qualifier(CACHE_COINLIB_EXCHANGER) Cache cache) {
        this.supportedService = supportedService;
        this.apiUrlCoin = apiUrlCoin;
        this.apiKey = apiKey;
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COINLIB;
    }

    @Override
    public BigDecimal getRate(String currencySymbol, BaseCurrency baseCurrency) {
        Map<BaseCurrency, Coin> data = cache.get(currencySymbol, () -> getDataFromMarket(currencySymbol));
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Coinlib server is not available");
            return BigDecimal.ZERO;
        }
        Coin response = data.get(baseCurrency);

        return nonNull(response) ? BigDecimal.valueOf(response.price) : BigDecimal.ZERO;
    }

    private Map<BaseCurrency, Coin> getDataFromMarket(String currencySymbol) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        List<CompletableFuture<Pair<BaseCurrency, Coin>>> future = Stream.of(BaseCurrency.values())
                .map(value ->
                        CompletableFuture.supplyAsync(() -> Pair.of(value, getDataFromMarketByBaseCurrency(currencySymbol, value)), executor)
                                .exceptionally(ex -> {
                                    log.error("Get data from market failed", ex);
                                    return Pair.of(value, null);
                                }))
                .collect(toList());

        Map<BaseCurrency, Coin> collect = future.stream()
                .map(CompletableFuture::join)
                .filter(pair -> nonNull(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));

        ExecutorUtil.shutdownExecutor(executor);

        return collect;
    }

    private Coin getDataFromMarketByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", apiKey);
        requestParameters.add("symbol", supportedService.getSearchId(currencySymbol));
        requestParameters.add("pref", baseCurrency.name());

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlCoin)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<Coin> responseEntity = restTemplate.getForEntity(builder.toUriString(), Coin.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.error("Coinlib server is not available");
            return null;
        }
        return responseEntity.getBody();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class Coin {

        double price;
    }
}