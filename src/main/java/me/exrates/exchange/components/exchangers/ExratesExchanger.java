package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
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
import java.util.Collections;
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
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_EXRATES_EXCHANGER;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

@Slf4j
@Lazy
@Component("exratesExchanger")
public class ExratesExchanger implements Exchanger {

    private String apiUrlTicker;

    private final Cache cache;
    private final RestTemplate restTemplate;

    public ExratesExchanger(@Value("${exchangers.exrates.api-url.ticker}") String apiUrlTicker,
                            @Qualifier(CACHE_EXRATES_EXCHANGER) Cache cache) {
        this.apiUrlTicker = apiUrlTicker;
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.EXRATES;
    }

    @Override
    public BigDecimal getRate(String currencySymbol, BaseCurrency baseCurrency) {
        Map<BaseCurrency, List<ExratesData>> data = cache.get(currencySymbol, () -> getDataFromMarket(currencySymbol));
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Exrates server is not available");
            return BigDecimal.ZERO;
        }
        List<ExratesData> markets = data.get(baseCurrency);
        if (isEmpty(markets)) {
            return BigDecimal.ZERO;
        }
        ExratesData response = markets.get(0);

        return nonNull(response) ? BigDecimal.valueOf(response.last) : BigDecimal.ZERO;
    }

    private Map<BaseCurrency, List<ExratesData>> getDataFromMarket(String currencySymbol) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        List<CompletableFuture<Pair<BaseCurrency, List<ExratesData>>>> future = Stream.of(BaseCurrency.values())
                .map(value ->
                        CompletableFuture.supplyAsync(() -> Pair.of(value, getDataFromMarketByBaseCurrency(currencySymbol, value)), executor)
                                .exceptionally(ex -> {
                                    log.error("Get data from market failed", ex);
                                    return Pair.of(value, Collections.emptyList());
                                }))
                .collect(toList());

        Map<BaseCurrency, List<ExratesData>> collect = future.stream()
                .map(CompletableFuture::join)
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));

        ExecutorUtil.shutdownExecutor(executor);

        return collect;
    }

    private List<ExratesData> getDataFromMarketByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("currency_pair", String.format("%s_%s", currencySymbol.toLowerCase(), baseCurrency.name().toLowerCase()));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlTicker)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<ExratesData[]> responseEntity = restTemplate.getForEntity(builder.toUriString(), ExratesData[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.error("Exrates server is not available");
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
