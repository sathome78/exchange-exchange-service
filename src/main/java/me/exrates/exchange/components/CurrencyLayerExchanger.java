package me.exrates.exchange.components;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_CURRENCY_LAYER_EXCHANGER;

@Slf4j
@Component("currencyLayerExchanger")
public class CurrencyLayerExchanger implements Exchanger {

    private static final String CURRENCY_LAYER_API_URL = "http://www.apilayer.net/api";
    private static final String CURRENCY_LAYER_API_KEY = "59705b87d908ba5a93492fb14521d612";

    private static final String ALL = "ALL";

    private final Cache cache;
    private final RestTemplate restTemplate;

    public CurrencyLayerExchanger(@Qualifier(CACHE_CURRENCY_LAYER_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.FREE_CURRENCY;
    }

    @Override
    public BigDecimal getRate(String currencyName, BaseCurrency currency) {
        Map<String, String> data = cache.get(ALL, this::getDataFromMarket);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from CurrencyLayer not available");
            return BigDecimal.ZERO;
        }
        Pair<String, BigDecimal> pair = data.entrySet().stream()
                .filter(entry -> entry.getKey().substring(3).equals(currencyName))
                .map(entry -> Pair.of(entry.getKey(), new BigDecimal(entry.getValue())))
                .findFirst()
                .orElse(null);

        return nonNull(pair) ? pair.getValue() : BigDecimal.ZERO;
    }

    private Map<String, String> getDataFromMarket() throws ExchangerException {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("access_key", CURRENCY_LAYER_API_KEY);

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(CURRENCY_LAYER_API_URL + "/live")
                .queryParams(requestParameters)
                .build();

        ResponseEntity<FreeCurrencyResponse> responseEntity = restTemplate.getForEntity(builder.toUriString(), FreeCurrencyResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("CurrencyLayer server is not available");
        }
        FreeCurrencyResponse body = responseEntity.getBody();

        return nonNull(body) && nonNull(body.quotes) && !body.quotes.rates.isEmpty()
                ? body.quotes.rates
                : Collections.emptyMap();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class FreeCurrencyResponse {

        @JsonProperty("quotes")
        @Valid
        Quotes quotes;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class Quotes {

        Map<String, String> rates = Maps.newTreeMap();

        @JsonAnySetter
        void setRates(String key, String value) {
            rates.put(key, value);
        }
    }
}
