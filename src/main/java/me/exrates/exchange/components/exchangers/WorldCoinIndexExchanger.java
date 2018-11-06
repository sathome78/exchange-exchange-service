package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;
import static me.exrates.exchange.utils.CollectionUtil.isNotEmpty;

@Slf4j
@Lazy
@Component("worldCoinIndexExchanger")
public class WorldCoinIndexExchanger implements Exchanger {

    private String apiUrlTicker;
    private String apiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public WorldCoinIndexExchanger(@Value("${exchangers.worldcoinindex.api-url.ticker}") String apiUrlTicker,
                                   @Value("${exchangers.worldcoinindex.api-key}") String apiKey) {
        this.apiUrlTicker = apiUrlTicker;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.WORLD_COIN_INDEX;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        Map<BaseCurrency, List<Market>> data = getDataFromMarket(currencySymbol);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from WorldCoinIndex server is not available");
            return null;
        }
        List<Market> btcData = data.get(BaseCurrency.BTC);
        List<Market> usdData = data.get(BaseCurrency.USD);
        if (isEmpty(btcData) || isEmpty(usdData)) {
            return null;
        }
        final Market btcRate = btcData.get(0);
        final Market usdRate = usdData.get(0);

        return nonNull(btcRate) && nonNull(usdRate)
                ? CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .btcRate(BigDecimal.valueOf(btcRate.price))
                .usdRate(BigDecimal.valueOf(usdRate.price))
                .build()
                : null;
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

        ResponseEntity<WorldCoinIndexData> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(builder.toUriString(), WorldCoinIndexData.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("WorldCoinIndex server is not available");
            }
        } catch (Exception ex) {
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