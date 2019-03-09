package me.exrates.exchange.api;

import com.fasterxml.jackson.core.type.TypeReference;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.repositories.CurrencyRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@Ignore
public class ExchangeApiTest extends AbstractTest {

    private static final String TEST_COIN = "TEST-COIN";

    private static final String ABTC = "ABTC";
    private static final String DASH = "DASH";
    private static final String BRB = "BRB";
    private static final String VND = "VND";
    private static final String eMTV = "eMTV";

    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    public void endToEndCreateNewCurrencyTest() throws Exception {
        //create new currency
        CurrencyForm currency = CurrencyForm.builder()
                .symbol(TEST_COIN)
                .exchangerType(ExchangerType.EXRATES)
                .exchangerSymbol(TEST_COIN + ExchangerType.EXRATES.name())
                .btcRate(BigDecimal.ONE)
                .usdRate(BigDecimal.TEN)
                .build();
        MvcResult mvcResult = mockMvc.perform(post("/currency/create")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsBytes(currency))
        )
                .andExpect(status().isOk())
                .andReturn();

        CurrencyDto newCurrency = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CurrencyDto.class);

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
        mockMvc.perform(put("/currency/update")
                .param("currency_symbol", TEST_COIN)
                .param("exchanger_type", ExchangerType.COIN_MARKET_CUP.name())
                .param("exchanger_symbol", TEST_COIN + ExchangerType.COIN_MARKET_CUP.name())
        )
                .andExpect(status().isOk())
                .andReturn();

        Currency one = currencyRepository.getOne(TEST_COIN);

        assertNotNull(one);
        assertNotNull(one.getExchangerType());
        assertEquals(ExchangerType.COIN_MARKET_CUP, one.getExchangerType());
        assertEquals(TEST_COIN + ExchangerType.COIN_MARKET_CUP.name(), one.getExchangerSymbol());

        //delete new currency
        mockMvc.perform(delete("/currency/delete")
                .param("currency_symbol", TEST_COIN)
        )
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void getRatesByExchangerTypesTest() throws Exception {
        //Coinlib coin
        MvcResult mvcResult = mockMvc.perform(get("/currency/rates/{currency_symbol}", ABTC))
                .andExpect(status().isOk())
                .andReturn();

        CurrencyDto currency = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CurrencyDto.class);

        assertNotNull(currency);
        assertEquals(ABTC, currency.getSymbol());
        assertEquals(ExchangerType.COINLIB, currency.getExchangerType());

        //CoinMarketCup coin
        mvcResult = mockMvc.perform(get("/currency/rates/{currency_symbol}", DASH))
                .andExpect(status().isOk())
                .andReturn();

        currency = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CurrencyDto.class);

        assertNotNull(currency);
        assertEquals(DASH, currency.getSymbol());
        assertEquals(ExchangerType.COIN_MARKET_CUP, currency.getExchangerType());

        //Exrates coin
        mvcResult = mockMvc.perform(get("/currency/rates/{currency_symbol}", BRB))
                .andExpect(status().isOk())
                .andReturn();

        currency = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CurrencyDto.class);

        assertNotNull(currency);
        assertEquals(BRB, currency.getSymbol());
        assertEquals(ExchangerType.EXRATES, currency.getExchangerType());

        //FreeCurrency coin
        mvcResult = mockMvc.perform(get("/currency/rates/{currency_symbol}", VND))
                .andExpect(status().isOk())
                .andReturn();

        currency = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CurrencyDto.class);

        assertNotNull(currency);
        assertEquals(VND, currency.getSymbol());
        assertEquals(ExchangerType.FREE_CURRENCY, currency.getExchangerType());

        //WorldCoinIndex coin
        mvcResult = mockMvc.perform(get("/currency/rates/{currency_symbol}", eMTV))
                .andExpect(status().isOk())
                .andReturn();

        currency = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CurrencyDto.class);

        assertNotNull(currency);
        assertEquals(eMTV, currency.getSymbol());
        assertEquals(ExchangerType.WORLD_COIN_INDEX, currency.getExchangerType());
    }

    @Test
    public void getAllRatesTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/currency/rates/all"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, CurrencyDto> all = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Map<String, CurrencyDto>>() {
        });

        assertNotNull(all);
        assertFalse(all.isEmpty());
    }

    @Test
    public void getRatesByTypeTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/currency/rates/type/{currency_type}", "crypto"))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, CurrencyDto> allByType = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Map<String, CurrencyDto>>() {
        });

        assertNotNull(allByType);
        assertFalse(allByType.isEmpty());

        mvcResult = mockMvc.perform(get("/currency/rates/type/{currency_type}", "test"))
                .andExpect(status().isOk())
                .andReturn();

        allByType = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<Map<String, CurrencyDto>>() {
        });

        assertNotNull(allByType);
        assertTrue(allByType.isEmpty());
    }
}
