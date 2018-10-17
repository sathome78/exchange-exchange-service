package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
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

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_WORLD_COIN_INDEX_EXCHANGER;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

@Slf4j
@Lazy
@Component("worldCoinIndexExchanger")
public class WorldCoinIndexExchanger implements Exchanger {

    private String apiUrlTicker;
    private String apiKey;

    private final Cache cache;
    private final RestTemplate restTemplate;

    public WorldCoinIndexExchanger(@Value("${exchangers.worldcoinindex.api-url.ticker}") String apiUrlTicker,
                                   @Value("${exchangers.worldcoinindex.api-key}") String apiKey,
                                   @Qualifier(CACHE_WORLD_COIN_INDEX_EXCHANGER) Cache cache) {
        this.apiUrlTicker = apiUrlTicker;
        this.apiKey = apiKey;
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.WORLD_COIN_INDEX;
    }

    @Override
    public BigDecimal getRate(String currencySymbol, BaseCurrency baseCurrency) {
        Map<BaseCurrency, List<Market>> data = cache.get(currencySymbol, () -> getDataFromMarket(currencySymbol));
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from WorldCoinIndex server is not available");
            return BigDecimal.ZERO;
        }
        List<Market> markets = data.get(baseCurrency);
        if (isEmpty(markets)) {
            return BigDecimal.ZERO;
        }
        Market response = markets.get(0);

        return nonNull(response) ? BigDecimal.valueOf(response.price) : BigDecimal.ZERO;
    }

    private Map<BaseCurrency, List<Market>> getDataFromMarket(String currencySymbol) {
        return Stream.of(BaseCurrency.values())
                .map(value -> Pair.of(value, getDataFromMarketByBaseCurrency(currencySymbol, value)))
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private List<Market> getDataFromMarketByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", apiKey);
        requestParameters.add("label", currencySymbol + BaseCurrency.BTC.name());
        requestParameters.add("fiat", baseCurrency.name());

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlTicker)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<WorldCoinIndexData> responseEntity = restTemplate.getForEntity(builder.toUriString(), WorldCoinIndexData.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.error("WorldCoinIndex server is not available");
            return Collections.emptyList();
        }
        WorldCoinIndexData body = responseEntity.getBody();

        return nonNull(body) && isNotEmpty(body.markets) ? body.markets : Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class WorldCoinIndexData {

        @JsonProperty("Markets")
        @Valid
        List<Market> markets;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class Market {

        @JsonProperty("Price")
        double price;
    }
}