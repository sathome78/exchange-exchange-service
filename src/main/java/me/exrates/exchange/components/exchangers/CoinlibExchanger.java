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
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Lazy
@Component("coinlibExchanger")
public class CoinlibExchanger implements Exchanger {

    private String apiUrlCoin;
    private String apiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public CoinlibExchanger(@Value("${exchangers.coinlib.api-url.coin}") String apiUrlCoin,
                            @Value("${exchangers.coinlib.api-key}") String apiKey) {
        this.apiUrlCoin = apiUrlCoin;
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COINLIB;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        Map<BaseCurrency, CoinlibData> data = getDataFromMarket(currencySymbol);
        if (isNull(data) || data.isEmpty()) {
            log.info("Data from Coinlib server is not available");
            return null;
        }
        final CoinlibData btcRate = data.get(BaseCurrency.BTC);
        final CoinlibData usdRate = data.get(BaseCurrency.USD);

        return nonNull(btcRate) && nonNull(usdRate)
                ? CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .btcRate(BigDecimal.valueOf(btcRate.price))
                .usdRate(BigDecimal.valueOf(usdRate.price))
                .build()
                : null;
    }

    private Map<BaseCurrency, CoinlibData> getDataFromMarket(String currencySymbol) {
        return Stream.of(BaseCurrency.values())
                .map(value -> Pair.of(value, getDataFromMarketByBaseCurrency(currencySymbol, value)))
                .filter(pair -> nonNull(pair.getValue()))
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private CoinlibData getDataFromMarketByBaseCurrency(String currencySymbol, BaseCurrency baseCurrency) {
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add("key", apiKey);
        requestParameters.add("symbol", currencySymbol);
        requestParameters.add("pref", baseCurrency.name());

        UriComponents builder = UriComponentsBuilder
                .fromHttpUrl(apiUrlCoin)
                .queryParams(requestParameters)
                .build();

        ResponseEntity<CoinlibData> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(builder.toUriString(), CoinlibData.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("Coinlib server is not available");
            }
        } catch (Exception ex) {
            return null;
        }
        return responseEntity.getBody();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CoinlibData {

        double price;
    }
}