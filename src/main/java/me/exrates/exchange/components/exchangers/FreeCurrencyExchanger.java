package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
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
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_FREE_CURRENCY_EXCHANGER;

@Slf4j
@Lazy
@Component("currencyLayerExchanger")
public class FreeCurrencyExchanger implements Exchanger {

    private static final String FREE_CURRENCY_API_URL = "https://free.currencyconverterapi.com/api/v6";

    private final Cache cache;
    private final RestTemplate restTemplate;

    public FreeCurrencyExchanger(@Qualifier(CACHE_FREE_CURRENCY_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.FREE_CURRENCY;
    }

    @Override
    public BigDecimal getRate(String currencyName, BaseCurrency currency) {
        Map<String, Rate> data = cache.get(currencyName, () -> getDataFromMarket(currencyName));
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from FreeCurrency server is not available");
            return BigDecimal.ZERO;
        }
        return data.entrySet().stream()
                .filter(entry -> currency.name().equals(entry.getKey().split("_")[1]))
                .map(entry -> new BigDecimal(entry.getValue().val))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private Map<String, Rate> getDataFromMarket(String currencyName) {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("compact", "y");
        requestParameters.add("q", String.format("%s_BTC,%s_USD", currencyName, currencyName));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(FREE_CURRENCY_API_URL + "/convert")
                .queryParams(requestParameters)
                .build();

        ResponseEntity<FreeCurrencyResponse> responseEntity = restTemplate.getForEntity(builder.toUriString(), FreeCurrencyResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("FreeCurrency server is not available");
        }
        FreeCurrencyResponse body = responseEntity.getBody();

        return nonNull(body) && !body.rates.isEmpty()
                ? body.rates
                : Collections.emptyMap();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class FreeCurrencyResponse {

        Map<String, Rate> rates = Maps.newTreeMap();

        @JsonAnySetter
        void setRates(String key, Rate value) {
            rates.put(key, value);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class Rate {

        String val;
    }
}
