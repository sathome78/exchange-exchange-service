package me.exrates.exchange.components.exchangers;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.nonNull;
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_COINMARKETCUP_ALL;

@Slf4j
@Lazy
@Component("coinMarketCupExchanger")
public class CoinMarketCupExchanger implements Exchanger {

    private static final String ALL = "ALL";

    private String apiUrl;

    private final RestTemplate restTemplate;
    private final Cache cache;

    @Autowired
    public CoinMarketCupExchanger(@Value("${exchangers.coinmarketcup.api-url.all}") String apiUrl,
                                  @Qualifier(CACHE_COINMARKETCUP_ALL) Cache cache) {
        this.apiUrl = apiUrl;

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        this.restTemplate = new RestTemplate(requestFactory);

        this.cache = cache;
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COIN_MARKET_CUP;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        String data = getDataFromCache();
        if (StringUtils.isEmpty(data)) {
            log.info("Data from Coinmarketcup server is not available");
            return null;
        }
        int i = data.indexOf("<a href=\"/currencies/bitcoin/markets/\" class=\"cmc-link\">$");
        if (i == -1) {
            log.info(String.format("Rates for %s is not available", currencySymbol));
            return null;
        }
        String firstSubstring = data.substring(i + "<a href=\"/currencies/bitcoin/markets/\" class=\"cmc-link\">$".length());
        String btcUsdRateSubstring = firstSubstring.substring(0, firstSubstring.indexOf("<")).replaceAll(",", "");

        final Double btcUsdRate = Double.valueOf(btcUsdRateSubstring);

        i = data.indexOf(String.format("<a href=\"/currencies/%s/markets/\" class=\"cmc-link\">$", currencySymbol));
        if (i == -1) {
            log.info(String.format("Rates for %s is not available", currencySymbol));
            return null;
        }
        firstSubstring = data.substring(i + String.format("<a href=\"/currencies/%s/markets/\" class=\"cmc-link\">$", currencySymbol).length());
        String usdRateSubstring = firstSubstring.substring(0, firstSubstring.indexOf("<")).replaceAll(",", "");

        final Double usdRate = Double.valueOf(usdRateSubstring);

        return CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .btcRate(new BigDecimal(usdRate / btcUsdRate).setScale(8, RoundingMode.HALF_UP))
                .usdRate(new BigDecimal(usdRate).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private String getDataFromCache() {
        return cache.get(ALL, this::getDataFromMarket);
    }

    private String getDataFromMarket() {
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(apiUrl, String.class);
            if (responseEntity.getStatusCodeValue() != 200) {
                throw new ExchangerException("CoinMarketCup server is not available");
            }
        } catch (Exception ex) {
            log.warn("Error {}: {}", "COIN_MARKET_CUP", ex.getMessage());
            return StringUtils.EMPTY;
        }
        String body = responseEntity.getBody();

        return nonNull(body)
                ? body
                : StringUtils.EMPTY;
    }
}