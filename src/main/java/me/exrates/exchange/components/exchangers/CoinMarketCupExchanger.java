package me.exrates.exchange.components.exchangers;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.Exchanger;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.exrates.exchange.configurations.CacheConfiguration.CACHE_COINMARKETCUP_ALL;

@Slf4j
@Lazy
@Component("coinMarketCupExchanger")
public class CoinMarketCupExchanger implements Exchanger {

    private static final String ALL = "ALL";

    private String apiUrl;

    private final WebDriver driver;
    private final Cache cache;

    @Autowired
    public CoinMarketCupExchanger(@Value("${exchangers.coinmarketcup.api-url}") String apiUrl,
                                  WebDriver driver,
                                  @Qualifier(CACHE_COINMARKETCUP_ALL) Cache cache) {
        this.driver = driver;
        this.cache = cache;
        this.apiUrl = apiUrl;
    }

    @Override
    public ExchangerType getExchangerType() {
        return ExchangerType.COIN_MARKET_CUP;
    }

    @Override
    public CurrencyDto getRate(String currencySymbol) {
        List<Element> data = getDataFromCache();
        if (data.isEmpty()) {
            log.info("Data from Coinmarketcup server is not available");
            return null;
        }
        Double btcUsdRate = data.stream()
                .map(element -> {
                    Element elementByClass = element.getElementsByClass("cmc-table__cell--sort-by__price").stream().findFirst().get();
                    Node node = elementByClass.childNodes().get(0);
                    String html = node.outerHtml();
                    String text = "<a href=\"/currencies/bitcoin/markets/\" class=\"cmc-link\">$";

                    return getRate(html, text);
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (Objects.isNull(btcUsdRate)) {
            log.info("Rates for bitcoin is not available");
            return null;
        }

        Double usdRate = data.stream()
                .map(element -> {
                    Element elementByClass = element.getElementsByClass("cmc-table__cell--sort-by__price").stream().findFirst().get();
                    Node node = elementByClass.childNodes().get(0);
                    String html = node.outerHtml();
                    String text = String.format("<a href=\"/currencies/%s/markets/\" class=\"cmc-link\">$", currencySymbol);

                    return getRate(html, text);
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (Objects.isNull(usdRate)) {
            log.info("Rates for {} is not available", currencySymbol);
            return null;
        }

        return CurrencyDto.builder()
                .symbol(currencySymbol)
                .exchangerType(getExchangerType())
                .btcRate(new BigDecimal(usdRate / btcUsdRate).setScale(8, RoundingMode.HALF_UP))
                .usdRate(new BigDecimal(usdRate).setScale(8, RoundingMode.HALF_UP))
                .build();
    }

    private Double getRate(String html, String text) {
        if (html.contains(text)) {
            int i = html.indexOf(text);
            String firstSubstring = html.substring(i + text.length());
            String btcUsdRateSubstring = firstSubstring.substring(0, firstSubstring.indexOf("<")).replaceAll(",", "");

            return Double.valueOf(btcUsdRateSubstring);
        }
        return null;
    }

    private List<Element> getDataFromCache() {
        return cache.get(ALL, this::getDataFromMarket);
    }

    private List<Element> getDataFromMarket() {
        return getAllElementsByPage(1, new ArrayList<>());
    }

    private List<Element> getAllElementsByPage(int page, List<Element> bufferList) {
        final String preparedUrl = apiUrl + String.valueOf(page);

        driver.get(preparedUrl);

        String pageSource = driver.getPageSource();

        Document document = Jsoup.parse(pageSource);

        Element body = document.body();

        List<Element> elements = new ArrayList<>(body.getElementsByClass("cmc-table-row"));
        bufferList.addAll(elements);

        if (elements.size() == 0) {
            return bufferList;
        }
        return getAllElementsByPage(page + 1, bufferList);
    }
}