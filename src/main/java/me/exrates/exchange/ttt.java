package me.exrates.exchange;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.exceptions.ExchangerException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;
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

@Slf4j
public class ttt {

    public static void main(String[] args) throws ExchangerException {
        BigDecimal eth = getRate("RUB");
        BigDecimal eur = getRate("EUR");
        BigDecimal uah = getRate("UAH");
    }

    private static BigDecimal getRate(String currencyName) throws ExchangerException {
        Map<String, String> data = getDataFromMarket();
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

    private static Map<String, String> getDataFromMarket() throws ExchangerException {
        final MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("access_key", "59705b87d908ba5a93492fb14521d612");
        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl("http://www.apilayer.net/api/live")
                .queryParams(requestParameters)
                .build();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<FreeCurrencyResponse> responseEntity = restTemplate.getForEntity(builder.toUriString(), FreeCurrencyResponse.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("FreeCurrency server is not available");
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
