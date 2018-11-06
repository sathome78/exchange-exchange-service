package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.components.exchangers.ExratesExchanger;
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
public class ExratesExchangerTest {

    private static final String CURRENCY_SYMBOL = "BRB";

    @Autowired
    private ExratesExchanger exratesExchanger;

    @Test
    public void getRateTest() {
        log.info("ExratesExchangerTest - getRateTest() start...");
        CurrencyDto currencyDto = exratesExchanger.getRate(CURRENCY_SYMBOL);

        assertNotNull(currencyDto);
        assertNotNull(currencyDto.getUsdRate());
        assertNotNull(currencyDto.getBtcRate());
        assertEquals(ExchangerType.EXRATES, currencyDto.getExchangerType());
        log.info("ExratesExchangerTest - getRateTest() end");
    }
}
