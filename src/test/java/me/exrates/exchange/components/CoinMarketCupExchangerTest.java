package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.exchangers.CoinMarketCupExchanger;
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
public class CoinMarketCupExchangerTest {

    private static final String CURRENCY_SYMBOL = "bitcoin";

    @Autowired
    private CoinMarketCupExchanger coinMarketCupExchanger;

    @Test
    public void getRateTest() {
        log.info("CoinMarketCupExchangerTest - getRateTest() start...");
        CurrencyDto currencyDto = coinMarketCupExchanger.getRate(CURRENCY_SYMBOL);

        assertNotNull(currencyDto);
        assertNotNull(currencyDto.getUsdRate());
        assertNotNull(currencyDto.getBtcRate());
        assertEquals(ExchangerType.COIN_MARKET_CUP, currencyDto.getExchangerType());
        log.info("CoinMarketCupExchangerTest - getRateTest() end");
    }
}
