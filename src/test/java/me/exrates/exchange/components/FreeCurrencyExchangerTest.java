package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.exchangers.FreeCurrencyExchanger;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FreeCurrencyExchangerTest {

    private static final String CURRENCY_SYMBOL = "USD";

    @Autowired
    private FreeCurrencyExchanger freeCurrencyExchanger;

    @Test
    public void getRateTest() {
        log.info("FreeCurrencyExchangerTest - getRateTest() start...");
        CurrencyDto currencyDto = freeCurrencyExchanger.getRate(CURRENCY_SYMBOL);

        assertNotNull(currencyDto);
        assertNotNull(currencyDto.getUsdRate());
        assertNotNull(currencyDto.getBtcRate());
        assertEquals(ExchangerType.FREE_CURRENCY, currencyDto.getExchangerType());
        log.info("FreeCurrencyExchangerTest - getRateTest() end");
    }
}
