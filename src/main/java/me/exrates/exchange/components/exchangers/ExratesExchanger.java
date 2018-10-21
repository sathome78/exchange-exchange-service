package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

@Slf4j
@Lazy
@Component("exratesExchanger")
public class ExratesExchanger implements Exchanger {

    private String apiUrlTicker;

    private final RestTemplate restTemplate;

    @Autowired
    public ExratesExchanger(@Value("${exchangers.exrates.api-url.ticker}") String apiUrlTicker) {
        this.apiUrlTicker = apiUrlTicker;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.EXRATES;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        Map<BaseCurrency, List<ExratesData>> data = getDataFromMarket(currencySymbol);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Exrates server is not available");
            return null;
        }
        List<ExratesData> btcData = data.get(BaseCurrency.BTC);
        List<ExratesData> usdData = data.get(BaseCurrency.USD);
        if (isEmpty(btcData) || isEmpty(usdData)) {
            return null;
        }
        final ExratesData btcRate = btcData.get(0);
        final ExratesData usdRate = usdData.get(0);

        return nonNull(btcRate) && nonNull(usdRate)
                ? CurrencyDto.builder()
                .symbol(currencySymbol)
                .type(getExchangerType())
                .btcRate(BigDecimal.valueOf(btcRate.last))
                .usdRate(BigDecimal.valueOf(usdRate.last))
                .build()
                : null;
    }

    private Map<BaseCurrency, List<ExratesData>> getDataFromMarket(String currencySymbol) {
        return Stream.of(BaseCurrency.values())
                .map(value -> Pair.of(value, getDataFromMarketByBaseCurrency(currencySymbol, value)))
                .filter(pair -> isNotEmpty(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private List<ExratesData> getDataFromMarketByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("currency_pair", String.format("%s_%s", currencySymbol.toLowerCase(), baseCurrency.name().toLowerCase()));

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlTicker)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<ExratesData[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(builder.toUriString(), ExratesData[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("Exrates server is not available");
            }
        } catch (Exception ex) {
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
