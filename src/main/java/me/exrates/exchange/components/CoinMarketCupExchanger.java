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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_COIN_MARKET_CUP_EXCHANGER;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

@Slf4j
@Component("coinMarketCupExchanger")
public class CoinMarketCupExchanger implements Exchanger {

    private static final String COIN_MARKET_CUP_API_URL = "https://api.coinmarketcap.com/v1/ticker";

    private static final String ALL = "All";

    private final Cache cache;
    private final RestTemplate restTemplate;

    public CoinMarketCupExchanger(@Qualifier(CACHE_COIN_MARKET_CUP_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COIN_MARKET_CUP;
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
        Set<CoinMarketCup> data = cache.get(ALL, this::getDataFromMarket);
        if (isEmpty(data)) {
            log.info("Data from Coinmarketcup not available");
            return BigDecimal.ZERO;
        }
        CoinMarketCup coinMarketCup = data.stream()
                .filter(ticker -> ticker.symbol.equals(currencyName))
                .findFirst()
                .orElse(null);
        switch (currency) {
            case USD:
                return nonNull(coinMarketCup) ? new BigDecimal(coinMarketCup.priceUSD) : BigDecimal.ZERO;
            case BTC:
                return nonNull(coinMarketCup) ? new BigDecimal(coinMarketCup.priceBTC) : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }

    private Set<CoinMarketCup> getDataFromMarket() throws ExchangerException {
        ResponseEntity<CoinMarketCup[]> responseEntity = restTemplate.getForEntity(COIN_MARKET_CUP_API_URL, CoinMarketCup[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("CoinMarketCup server is not available");
        }
        CoinMarketCup[] body = responseEntity.getBody();

        return nonNull(body) ? Stream.of(body).collect(toSet()) : Collections.emptySet();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CoinMarketCup {

        String symbol;
        @JsonProperty("price_usd")
        String priceUSD;
        @JsonProperty("price_btc")
        String priceBTC;
    }
}
