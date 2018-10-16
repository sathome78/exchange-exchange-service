package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
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

    private static final String EXRATES_API_URL = "https://exrates.me/openapi/v1/public";

    private final Cache cache;
    private final RestTemplate restTemplate;

    public ExratesExchanger(@Qualifier(CACHE_EXRATES_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.EXRATES;
    }

    @Override
    public BigDecimal getRate(String currencyName, BaseCurrency currency) {
        Map<BaseCurrency, List<ExratesResponse>> data = cache.get(currencyName, () -> getDataFromMarket(currencyName));
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Exrates server is not available");
            return BigDecimal.ZERO;
        }
        List<ExratesResponse> markets = data.get(currency);
        if (isEmpty(markets)) {
            return BigDecimal.ZERO;
        }
        ExratesResponse response = markets.get(0);

        return nonNull(response) ? BigDecimal.valueOf(response.price) : BigDecimal.ZERO;
    }

    private Map<BaseCurrency, List<ExratesResponse>> getDataFromMarket(String currencyName) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        List<CompletableFuture<Pair<BaseCurrency, List<ExratesResponse>>>> future = Stream.of(BaseCurrency.values())
                .map(value ->
                        CompletableFuture.supplyAsync(() -> Pair.of(value, getDataFromMarketByBaseCurrency(currencyName, value)), executor)
                                .exceptionally(ex -> {
                                    log.error("Get data from market failed", ex);
                                    return Pair.of(value, Collections.emptyList());
                                }))
                .collect(toList());

        Map<BaseCurrency, List<ExratesResponse>> collect = future.stream()
                .map(CompletableFuture::join)
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));

        ExecutorUtil.shutdownExecutor(executor);

        return collect;
    }

    private List<ExratesResponse> getDataFromMarketByBaseCurrency(String currencyName, BaseCurrency currency) {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("currency_pair", String.format("%s_%s", currencyName.toLowerCase(), currency.name().toLowerCase()));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(EXRATES_API_URL + "/ticker")
                .queryParams(requestParameters)
                .build();


        ResponseEntity<ExratesResponse[]> responseEntity = restTemplate.getForEntity(builder.toUriString(), ExratesResponse[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("Exrates server is not available");
        }
        ExratesResponse[] body = responseEntity.getBody();

        return nonNull(body) ? Stream.of(body).collect(toList()) : Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class ExratesResponse {

        @JsonProperty("last")
        double price;
    }
}
