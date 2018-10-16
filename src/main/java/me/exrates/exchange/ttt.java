package me.exrates.exchange;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.enums.BaseCurrency;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

@Slf4j
public class ttt {

    public static void main(String[] args) throws ExchangerException {
//        BigDecimal eth = getRate("ETH", BaseCurrency.BTC);
        Set<CoinMarketCup> dataFromMarket = getDataFromMarket();
    }

//    private static BigDecimal getRate(String currencyName, BaseCurrency currency) throws ExchangerException {
//        Set<CoinMarketCup> data = getDataFromMarket();
//        if (isEmpty(data)) {
//            log.info("Data from Coinmarketcup not available");
//            return BigDecimal.ZERO;
//        }
//        CoinMarketCup response = data.stream()
//                .filter(ticker -> ticker.symbol.equals(currencyName))
//                .findFirst()
//                .orElse(null);
//        switch (currency) {
//            case USD:
//                return nonNull(response) ? new BigDecimal(response.priceUSD) : BigDecimal.ZERO;
//            case BTC:
//                return nonNull(response) ? new BigDecimal(response.priceBTC) : BigDecimal.ZERO;
//            default:
//                return BigDecimal.ZERO;
//        }
//    }

    private static Set<CoinMarketCup> getDataFromMarket() throws ExchangerException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<CoinMarketCup[]> responseEntity = restTemplate.getForEntity("https://api.coinmarketcap.com/v1/ticker", CoinMarketCup[].class);
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

        String id;
        String symbol;
    }
}
