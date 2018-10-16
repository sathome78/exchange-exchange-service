package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.BaseCurrency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_COIN_MARKET_CUP_EXCHANGER;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

@Slf4j
@Lazy
@Component("coinMarketCupExchanger")
public class CoinMarketCupExchanger implements Exchanger {

    private static final String COIN_MARKET_CUP_API_URL = "https://api.coinmarketcap.com/v1";

    private final Map<String, String> codes;

    private final Cache cache;
    private final RestTemplate restTemplate;

    public CoinMarketCupExchanger(@Qualifier(CACHE_COIN_MARKET_CUP_EXCHANGER) Cache cache) {
        this.cache = cache;
        this.restTemplate = new RestTemplate();
        this.codes = getCodes();
    }

    private Map<String, String> getCodes() {
        List<CoinMarketCup> data;
        try {
            data = getDataFromMarket(null);
        } catch (ExchangerException ex) {
            log.warn("Data from Coinmarketcup server is not available");
            return Collections.emptyMap();
        }
        if (isEmpty(data)) {
            log.info("Data from Coinmarketcup server is not available");
            return Collections.emptyMap();
        }
        return data.stream()
                .collect(toMap(
                        d -> d.symbol,
                        d -> d.id,
                        (d1, d2) -> d1
                ));
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COIN_MARKET_CUP;
    }

    @Override
    public BigDecimal getRate(String currencyName, BaseCurrency currency) {
        List<CoinMarketCup> data = cache.get(currencyName, () -> getDataFromMarket(currencyName));
        if (isEmpty(data)) {
            log.info("Data from Coinmarketcup server is not available");
            return BigDecimal.ZERO;
        }
        CoinMarketCup response = data.get(0);

        switch (currency) {
            case USD:
                return nonNull(response) ? new BigDecimal(response.priceUSD) : BigDecimal.ZERO;
            case BTC:
                return nonNull(response) ? new BigDecimal(response.priceBTC) : BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }

    private List<CoinMarketCup> getDataFromMarket(String currencyName) {
        final String url = COIN_MARKET_CUP_API_URL
                + (nonNull(currencyName) ? "/ticker/" + codes.get(currencyName) : "/ticker");

        ResponseEntity<CoinMarketCup[]> responseEntity = restTemplate.getForEntity(url, CoinMarketCup[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("CoinMarketCup server is not available");
        }
        CoinMarketCup[] body = responseEntity.getBody();

        return nonNull(body) ? Stream.of(body).collect(toList()) : Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CoinMarketCup {

        String id;
        String symbol;
        @JsonProperty("price_usd")
        String priceUSD;
        @JsonProperty("price_btc")
        String priceBTC;
    }
}