package me.exrates.exchange;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.exrates.exchange.exceptions.ExchangerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

public class ttt {

    public static void main(String[] args) throws ExchangerException {
        Set<CoinMarketCup> data = getDataFromMarket();
    }

    private static Set<CoinMarketCup> getDataFromMarket() throws ExchangerException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CoinMarketCup[]> responseEntity = restTemplate.getForEntity("https://api.coinmarketcap.com/v1/ticker/", CoinMarketCup[].class);
        if (responseEntity.getStatusCodeValue() != 200) {
            throw new ExchangerException("CoinMarketCup server is not available");
        }
        CoinMarketCup[] body = responseEntity.getBody();

        return nonNull(body) ? Stream.of(body).collect(toSet()) : Collections.emptySet();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class CoinMarketCupResponse {

        CoinMarketCup[] response;
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
