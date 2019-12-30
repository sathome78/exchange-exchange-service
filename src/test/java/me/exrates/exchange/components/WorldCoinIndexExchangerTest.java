package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.exchangers.WorldCoinIndexExchanger;
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
public class WorldCoinIndexExchangerTest {

    private static final String CURRENCY_SYMBOL = "ECHT";

    @Autowired
    private WorldCoinIndexExchanger worldCoinIndexExchanger;

    @Test
    public void getRateTest() {
        log.info("WorldCoinIndexExchangerTest - getRateTest() start...");
        CurrencyDto currencyDto = worldCoinIndexExchanger.getRate(CURRENCY_SYMBOL);

        assertNotNull(currencyDto);
        assertNotNull(currencyDto.getUsdRate());
        assertNotNull(currencyDto.getBtcRate());
        assertEquals(ExchangerType.WORLD_COIN_INDEX, currencyDto.getExchangerType());
        log.info("WorldCoinIndexExchangerTest - getRateTest() end");
    }
}
