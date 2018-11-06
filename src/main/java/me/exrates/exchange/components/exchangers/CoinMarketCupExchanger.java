package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

@Slf4j
@Lazy
@Component("coinMarketCupExchanger")
public class CoinMarketCupExchanger implements Exchanger {

    private String apiUrlTicker;

    private final RestTemplate restTemplate;

    @Autowired
    public CoinMarketCupExchanger(@Value("${exchangers.coinmarketcup.api-url.ticker}") String apiUrlTicker) {
        this.apiUrlTicker = apiUrlTicker;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COIN_MARKET_CUP;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        List<CoinMarketCupData> data = getDataFromMarket(currencySymbol);
        if (isEmpty(data)) {
            log.info("Data from Coinmarketcup server is not available");
            return null;
        }
        final CoinMarketCupData response = data.get(0);

        return nonNull(response)
                ? CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .btcRate(new BigDecimal(response.priceBTC))
                .usdRate(new BigDecimal(response.priceUSD))
                .build()
                : null;
    }

    private List<CoinMarketCupData> getDataFromMarket(String currencySymbol) {
        ResponseEntity<CoinMarketCupData[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(apiUrlTicker + currencySymbol, CoinMarketCupData[].class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("CoinMarketCup server is not available");
            }
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        CoinMarketCupData[] body = responseEntity.getBody();

        return nonNull(body)
                ? Stream.of(body).collect(toList())
                : Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CoinMarketCupData {

        @JsonProperty("price_usd")
        String priceUSD;
        @JsonProperty("price_btc")
        String priceBTC;
    }
}