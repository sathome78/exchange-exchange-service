package me.exrates.exchange.api;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.entities.CurrencyHistory;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.dto.CurrencyHistoryDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.repositories.CurrencyHistoryRepository;
import me.exrates.exchange.repositories.CurrencyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExchangeApiTest {

    private static final String TEST_COIN = "TEST-COIN";

    private static final String BCS = "BCS";
    private static final String DASH = "DASH";
    private static final String BRB = "BRB";
    private static final String VND = "VND";
    private static final String eMTV = "eMTV";

    @Value("${exchange-api.url}")
    private String url;

    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private CurrencyHistoryRepository currencyHistoryRepository;

    private ExchangeApi exchangeApi;

    @Before
    public void setUp() {
        exchangeApi = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(ExchangeApi.class))
                .target(ExchangeApi.class, url);
    }

    @Test
    public void endToEndCreateNewCurrencyTest() {
        //create new currency
        CurrencyForm currency = CurrencyForm.builder()
                .symbol(TEST_COIN)
                .exchangerType(ExchangerType.EXRATES)
                .exchangerSymbol(TEST_COIN + ExchangerType.EXRATES.name())
                .btcRate(BigDecimal.ONE)
                .usdRate(BigDecimal.TEN)
                .build();
        CurrencyDto newCurrency = exchangeApi.createCurrency(currency);

        assertNotNull(newCurrency);
        assertNotNull(newCurrency.getSymbol());
        assertNotNull(newCurrency.getExchangerType());
        assertNotNull(newCurrency.getBtcRate());
        assertNotNull(newCurrency.getBtcRateUpdatedAt());
        assertNotNull(newCurrency.getUsdRate());
        assertNotNull(newCurrency.getUsdRateUpdatedAt());

        assertEquals(TEST_COIN, newCurrency.getSymbol());
        assertEquals(ExchangerType.EXRATES, newCurrency.getExchangerType());
        assertEquals(BigDecimal.ONE, newCurrency.getBtcRate());
        assertEquals(BigDecimal.TEN, newCurrency.getUsdRate());

        //update exchanger type
        exchangeApi.updateCurrency(TEST_COIN, ExchangerType.COIN_MARKET_CUP, TEST_COIN + ExchangerType.COIN_MARKET_CUP.name());

        Currency one = currencyRepository.getOne(TEST_COIN);

        assertNotNull(one);
        assertNotNull(one.getExchangerType());
        assertEquals(ExchangerType.COIN_MARKET_CUP, one.getExchangerType());
        assertEquals(TEST_COIN + ExchangerType.COIN_MARKET_CUP.name(), one.getExchangerSymbol());

        CurrencyHistory historyOne = CurrencyHistory.builder()
                .usdRate(one.getUsdRate())
                .btcRate(one.getBtcRate())
                .createdAt(LocalDate.now())
                .currency(one)
                .build();
        currencyHistoryRepository.save(historyOne);

        CurrencyHistory historyTwo = CurrencyHistory.builder()
                .usdRate(one.getUsdRate())
                .btcRate(one.getBtcRate())
                .createdAt(LocalDate.now())
                .currency(one)
                .build();
        currencyHistoryRepository.save(historyTwo);

        //get all history records
        List<CurrencyHistoryDto> ratesHistoryByCurrencySymbol = exchangeApi.getRatesHistoryByCurrencySymbol(TEST_COIN);

        assertNotNull(ratesHistoryByCurrencySymbol);
        assertFalse(ratesHistoryByCurrencySymbol.isEmpty());
        assertEquals(2, ratesHistoryByCurrencySymbol.size());

        //delete new currency
        exchangeApi.deleteCurrency(TEST_COIN);
    }

    @Test
    public void getRatesByExchangerTypesTest() {
        //Coinlib coin
        CurrencyDto currency = exchangeApi.getRatesByCurrencySymbol(BCS);

        assertNotNull(currency);
        assertEquals(BCS, currency.getSymbol());
        assertEquals(ExchangerType.COINLIB, currency.getExchangerType());

        //CoinMarketCup coin
        currency = exchangeApi.getRatesByCurrencySymbol(DASH);

        assertNotNull(currency);
        assertEquals(DASH, currency.getSymbol());
        assertEquals(ExchangerType.COIN_MARKET_CUP, currency.getExchangerType());

        //Exrates coin
        currency = exchangeApi.getRatesByCurrencySymbol(BRB);

        assertNotNull(currency);
        assertEquals(BRB, currency.getSymbol());
        assertEquals(ExchangerType.EXRATES, currency.getExchangerType());

        //FreeCurrency coin
        currency = exchangeApi.getRatesByCurrencySymbol(VND);

        assertNotNull(currency);
        assertEquals(VND, currency.getSymbol());
        assertEquals(ExchangerType.FREE_CURRENCY, currency.getExchangerType());

        //WorldCoinIndex coin
        currency = exchangeApi.getRatesByCurrencySymbol(eMTV);

        assertNotNull(currency);
        assertEquals(eMTV, currency.getSymbol());
        assertEquals(ExchangerType.WORLD_COIN_INDEX, currency.getExchangerType());
    }

    @Test
    public void getAllRatesTest() {
        Map<String, CurrencyDto> all = exchangeApi.getAllRates();

        assertNotNull(all);
        assertFalse(all.isEmpty());
    }
}
