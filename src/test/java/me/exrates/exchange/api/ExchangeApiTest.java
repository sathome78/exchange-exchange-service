package me.exrates.exchange.api;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.dto.RateDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
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

import static org.junit.Assert.assertEquals;
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

    @Value("${exchange-api.path}")
    private String path;

    @Autowired
    private CurrencyRepository currencyRepository;

    private ExchangeApi exchangeApi;

    @Before
    public void setUp() {
        exchangeApi = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(ExchangeApi.class))
                .target(ExchangeApi.class, url + path);
    }

    @Test
    public void endToEndCreateNewCurrencyTest() {
        //create new currency
        CurrencyForm currency = CurrencyForm.builder()
                .name(TEST_COIN)
                .type(ExchangerType.EXRATES)
                .btcRate(BigDecimal.ONE)
                .usdRate(BigDecimal.TEN)
                .build();
        CurrencyDto newCurrency = exchangeApi.createCurrency(currency);

        assertNotNull(newCurrency);
        assertNotNull(newCurrency.getName());
        assertNotNull(newCurrency.getType());
        assertNotNull(newCurrency.getBtcRate());
        assertNotNull(newCurrency.getBtcRateUpdatedAt());
        assertNotNull(newCurrency.getUsdRate());
        assertNotNull(newCurrency.getUsdRateUpdatedAt());

        assertEquals(TEST_COIN, newCurrency.getName());
        assertEquals(ExchangerType.EXRATES, newCurrency.getType());
        assertEquals(BigDecimal.ONE, newCurrency.getBtcRate());
        assertEquals(BigDecimal.TEN, newCurrency.getUsdRate());

        //update exchanger type
        exchangeApi.updateCurrencyType(TEST_COIN, ExchangerType.COIN_MARKET_CUP);

        Currency one = currencyRepository.getOne(TEST_COIN);

        assertNotNull(one);
        assertNotNull(one.getType());
        assertEquals(ExchangerType.COIN_MARKET_CUP, one.getType());

        //delete new currency
        exchangeApi.deleteCurrency(TEST_COIN);
    }

    @Test
    public void getRatesTest() {
        //Coinlib coin
        RateDto btcRate = exchangeApi.getBTCRate(BCS);
        RateDto usdRate = exchangeApi.getUSDRate(BCS);

        assertNotNull(btcRate);
        assertEquals(BCS, btcRate.getCurrencySymbol());
        assertNotNull(usdRate);
        assertEquals(BCS, usdRate.getCurrencySymbol());

        //CoinMarketCup coin
        btcRate = exchangeApi.getBTCRate(DASH);
        usdRate = exchangeApi.getUSDRate(DASH);

        assertNotNull(btcRate);
        assertEquals(DASH, btcRate.getCurrencySymbol());
        assertNotNull(usdRate);
        assertEquals(DASH, usdRate.getCurrencySymbol());

        //Exrates coin
        btcRate = exchangeApi.getBTCRate(BRB);
        usdRate = exchangeApi.getUSDRate(BRB);

        assertNotNull(btcRate);
        assertEquals(BRB, btcRate.getCurrencySymbol());
        assertNotNull(usdRate);
        assertEquals(BRB, usdRate.getCurrencySymbol());

        //FreeCurrency coin
        btcRate = exchangeApi.getBTCRate(VND);
        usdRate = exchangeApi.getUSDRate(VND);

        assertNotNull(btcRate);
        assertEquals(VND, btcRate.getCurrencySymbol());
        assertNotNull(usdRate);
        assertEquals(VND, usdRate.getCurrencySymbol());

        //WorldCoinIndex coin
        btcRate = exchangeApi.getBTCRate(eMTV);
        usdRate = exchangeApi.getUSDRate(eMTV);

        assertNotNull(btcRate);
        assertEquals(eMTV, btcRate.getCurrencySymbol());
        assertNotNull(usdRate);
        assertEquals(eMTV, usdRate.getCurrencySymbol());
    }
}
