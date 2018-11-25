package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.exchangers.CoinlibExchanger;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CoinlibExchangerTest {

    private static final String CURRENCY_SYMBOL = "ANY";

    @Autowired
    private CoinlibExchanger coinlibExchanger;

    @Test
    public void getRateTest() {
        log.info("CoinlibExchangerTest - getRateTest() start...");
        CurrencyDto currencyDto = coinlibExchanger.getRate(CURRENCY_SYMBOL);

        assertNotNull(currencyDto);
        assertNotNull(currencyDto.getUsdRate());
        assertNotNull(currencyDto.getBtcRate());
        assertEquals(ExchangerType.COINLIB, currencyDto.getExchangerType());
        log.info("CoinlibExchangerTest - getRateTest() end");
    }
}
