package me.exrates.exchange.components.exchangers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.entities.CoinmarketcupDictionary;
import me.exrates.exchange.exceptions.ExchangerException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.repositories.CoinmarketcupDictionaryRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
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
import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_COIN_MARKET_CUP_CODES;
import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

@Slf4j
@Lazy
@Component("coinMarketCupExchanger")
public class CoinMarketCupExchanger implements Exchanger {

    private String apiUrlTicker;

    private final Cache codesCache;

    private final CoinmarketcupDictionaryRepository dictionaryRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public CoinMarketCupExchanger(@Value("${exchangers.coinmarketcup.api-url.ticker}") String apiUrlTicker,
                                  CoinmarketcupDictionaryRepository dictionaryRepository,
                                  @Qualifier(CACHE_COIN_MARKET_CUP_CODES) Cache codesCache) {
        this.apiUrlTicker = apiUrlTicker;
        this.dictionaryRepository = dictionaryRepository;
        this.restTemplate = new RestTemplate();
        this.codesCache = codesCache;
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
                .type(getExchangerType())
                .btcRate(new BigDecimal(response.priceBTC))
                .usdRate(new BigDecimal(response.priceUSD))
                .build()
                : null;
    }

    private List<CoinMarketCupData> getDataFromMarket(String currencySymbol) {
        final String url = apiUrlTicker
                + (nonNull(currencySymbol) ? codesCache.get(currencySymbol, () -> getCode(currencySymbol)) : StringUtils.EMPTY);

        ResponseEntity<CoinMarketCupData[]> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(url, CoinMarketCupData[].class);
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

    private String getCode(String currencySymbol) {
        CoinmarketcupDictionary coinlibDictionary = dictionaryRepository.getByCurrencySymbolAndEnabledTrue(currencySymbol);
        return nonNull(coinlibDictionary) ? coinlibDictionary.getCoinmarketcupSymbol() : currencySymbol;
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