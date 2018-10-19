package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Lazy
@Component("currencyLayerExchanger")
public class FreeCurrencyExchanger implements Exchanger {

    private String apiUrlConvert;

    private final RestTemplate restTemplate;

    public FreeCurrencyExchanger(@Value("${exchangers.freecurrency.api-url.convert}") String apiUrlConvert) {
        this.apiUrlConvert = apiUrlConvert;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.FREE_CURRENCY;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        Map<String, Rate> data = getDataFromMarket(currencySymbol);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from FreeCurrency server is not available");
            return null;
        }
        Map<BaseCurrency, Rate> groupedByBaseCurrency = data.entrySet().stream()
                .map(entry -> Pair.of(getBaseCurrency(entry.getKey()), entry.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        final Rate btcRate = groupedByBaseCurrency.get(BaseCurrency.BTC);
        final Rate usdRate = groupedByBaseCurrency.get(BaseCurrency.USD);

        return nonNull(btcRate) && nonNull(usdRate)
                ? CurrencyDto.builder()
                .name(currencySymbol)
                .type(getExchangerType())
                .btcRate(new BigDecimal(btcRate.val))
                .usdRate(new BigDecimal(usdRate.val))
                .build()
                : null;
    }

    private BaseCurrency getBaseCurrency(String key) {
        return BaseCurrency.valueOf(key.split("_")[1]);
    }

    private Map<String, Rate> getDataFromMarket(String currencySymbol) {
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("compact", "y");
        requestParameters.add("q", String.format("%s_BTC,%s_USD", currencySymbol, currencySymbol));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlConvert)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<FreeCurrencyData> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(builder.toUriString(), FreeCurrencyData.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("FreeCurrency server is not available");
            }
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
        FreeCurrencyData body = responseEntity.getBody();

        return nonNull(body) && !body.rates.isEmpty() ? body.rates : Collections.emptyMap();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class FreeCurrencyData {

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
