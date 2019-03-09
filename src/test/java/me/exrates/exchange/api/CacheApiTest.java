package me.exrates.exchange.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.exrates.exchange.models.dto.CacheOrderStatisticDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@Ignore
public class CacheApiTest extends AbstractTest {

    private static final String BTC_USD = "BTC/USD";
    private static final String TEST_PAIR = "TEST/TEST";

    @Test
    public void getCacheByCurrencyPairSymbolFoundTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/cache/get")
                .param("currency_pair_symbol", BTC_USD)
        )
                .andExpect(status().isOk())
                .andReturn();

        CacheOrderStatisticDto statistic = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), CacheOrderStatisticDto.class);

        assertNotNull(statistic);
        assertEquals(BTC_USD, statistic.getCurrencyPairName());
    }

    @Test
    public void getCacheByCurrencyPairSymbolNotFoundTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/cache/get")
                .param("currency_pair_symbol", TEST_PAIR)
        )
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

        assertEquals(StringUtils.EMPTY, contentAsString);
    }

    @Test
    public void getAllCacheTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/cache/get/all"))
                .andExpect(status().isOk())
                .andReturn();

        List<CacheOrderStatisticDto> statisticList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<CacheOrderStatisticDto>>() {
        });

        assertNotNull(statisticList);
        assertFalse(statisticList.isEmpty());
    }
}
