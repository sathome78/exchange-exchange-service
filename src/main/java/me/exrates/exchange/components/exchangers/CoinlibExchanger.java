package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.utils.CollectionUtil;
import me.exrates.exchange.utils.ExecutorUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
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

    private static final String COINLIB_API_URL = "https://coinlib.io/api/v1";
    private static final String COINLIB_API_KEY = "a5c162aa66e69ee6";

    private static final int perPage = 100;

    private final Map<String, String> codes;

    private final Cache cache;
    private final RestTemplate restTemplate;

    public CoinlibExchanger(@Qualifier(CACHE_COINLIB_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
        this.codes = getCodes();
    }

    private Map<String, String> getCodes() {
        final int pages = getNumberOfCoins() / perPage;

        return IntStream.range(0, pages)
                .mapToObj(this::getCoins)
                .filter(CollectionUtil::isNotEmpty)
                .flatMap(List::stream)
                .collect(toMap(
                        d -> d.showSymbol,
                        d -> d.symbol,
                        (d1, d2) -> d1
                ));
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COINLIB;
    }

    @Override
    public BigDecimal getRate(String currencyName, BaseCurrency currency) {
        Map<BaseCurrency, Coin> data = cache.get(currencyName, () -> getDataFromMarket(currencyName));
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Coinlib server is not available");
            return BigDecimal.ZERO;
        }
        Coin response = data.get(currency);

        return nonNull(response) ? BigDecimal.valueOf(response.price) : BigDecimal.ZERO;
    }

    private Map<BaseCurrency, Coin> getDataFromMarket(String currencyName) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        List<CompletableFuture<Pair<BaseCurrency, Coin>>> future = Stream.of(BaseCurrency.values())
                .map(value ->
                        CompletableFuture.supplyAsync(() -> Pair.of(value, getDataFromMarketByBaseCurrency(currencyName, value)), executor)
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

    private Coin getDataFromMarketByBaseCurrency(String currencyName, BaseCurrency currency) {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", COINLIB_API_KEY);
        requestParameters.add("symbol", codes.get(currencyName));
        requestParameters.add("pref", currency.name());

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(COINLIB_API_URL + "/coin")
                .queryParams(requestParameters)
                .build();

        ResponseEntity<Coin> responseEntity = restTemplate.getForEntity(builder.toUriString(), Coin.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.warn("Coinlib server is not available");
            return null;
        }
        return responseEntity.getBody();
    }

    private List<Coin> getCoins(int page) {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", COINLIB_API_KEY);
        requestParameters.add("page", String.valueOf(page));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(COINLIB_API_URL + "/coinlist")
                .queryParams(requestParameters)
                .build();

        ResponseEntity<CoinlibResponse> responseEntity = restTemplate.getForEntity(builder.toUriString(), CoinlibResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.warn("Coinlib server is not available");
            return Collections.emptyList();
        }
        CoinlibResponse body = responseEntity.getBody();

        return nonNull(body) ? body.coins : Collections.emptyList();
    }

    private int getNumberOfCoins() {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", COINLIB_API_KEY);

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(COINLIB_API_URL + "/global")
                .queryParams(requestParameters)
                .build();

        ResponseEntity<StatisticData> responseEntity = restTemplate.getForEntity(builder.toUriString(), StatisticData.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.warn("Coinlib server is not available");
            return 0;
        }
        StatisticData body = responseEntity.getBody();

        return nonNull(body) ? body.coins : 0;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CoinlibResponse {

        @JsonProperty("coins")
        @Valid
        List<Coin> coins;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class Coin {

        String symbol;
        @JsonProperty("show_symbol")
        String showSymbol;
        double price;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class StatisticData {

        int coins;
    }
}
