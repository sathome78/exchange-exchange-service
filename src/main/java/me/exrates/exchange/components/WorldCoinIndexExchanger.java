package me.exrates.exchange.components;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_WORLD_COIN_INDEX_EXCHANGER;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

@Slf4j
@Component("worldCoinIndexExchanger")
public class WorldCoinIndexExchanger implements Exchanger {

    private static final String WORLD_COIN_INDEX_API_URL = "https://www.worldcoinindex.com/apiservice";
    private static final String WORLD_COIN_INDEX_API_KEY = "GGJokOdeHrwab8hR8AdVDSn6k3kg4P";

    private final Cache cache;
    private final RestTemplate restTemplate;

    public WorldCoinIndexExchanger(@Qualifier(CACHE_WORLD_COIN_INDEX_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.WORLD_COIN_INDEX;
    }

    @Override
    public BigDecimal getBTCRate(String currencyName) {
        return getRate(currencyName, BaseCurrency.BTC);
    }

    @Override
    public BigDecimal getUSDRate(String currencyName) {
        return getRate(currencyName, BaseCurrency.USD);
    }

    private BigDecimal getRate(String currencyName, BaseCurrency currency) {
        Set<WorldCoinIndexMarket> data = cache.get(currency.name(), () -> getDataFromMarket(currency));
        if (isEmpty(data)) {
            log.info("Data from WorldCoinIndex not available");
            return BigDecimal.ZERO;
        }
        WorldCoinIndexMarket response = data.stream()
                .filter(ticker -> ticker.label.split("/")[0].equals(currencyName))
                .findFirst()
                .orElse(null);

        return nonNull(response) ? BigDecimal.valueOf(response.price) : BigDecimal.ZERO;
    }

    private Set<WorldCoinIndexMarket> getDataFromMarket(BaseCurrency currency) throws ExchangerException {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", WORLD_COIN_INDEX_API_KEY);
        requestParameters.add("fiat", currency.name());

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(WORLD_COIN_INDEX_API_URL + "/v2getmarkets")
                .queryParams(requestParameters)
                .build();


        ResponseEntity<WorldCoinIndexResponse> responseEntity = restTemplate.getForEntity(builder.toUriString(), WorldCoinIndexResponse.class, requestParameters);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("WorldCoinIndex server is not available");
        }
        WorldCoinIndexResponse body = responseEntity.getBody();

        return nonNull(body) && isNotEmpty(body.markets) && isNotEmpty(body.markets.get(0))
                ? new HashSet<>(body.markets.get(0))
                : Collections.emptySet();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class WorldCoinIndexResponse {

        @JsonProperty("Markets")
        @Valid
        List<List<WorldCoinIndexMarket>> markets;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class WorldCoinIndexMarket {

        @JsonProperty("Label")
        String label;
        @JsonProperty("Price")
        double price;
    }
}
