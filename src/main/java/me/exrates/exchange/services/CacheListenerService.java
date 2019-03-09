package me.exrates.exchange.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.models.dto.CacheOrderStatisticDto;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static me.exrates.exchange.ExchangeConfiguration.JSON_MAPPER;

@Slf4j
@Component
public class CacheListenerService {

    private final CurrencyPairService currencyPairService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CacheListenerService(CurrencyPairService currencyPairService,
                                @Qualifier(JSON_MAPPER) ObjectMapper objectMapper) {
        this.currencyPairService = currencyPairService;
        this.objectMapper = objectMapper;
    }

    @Bean
    MessageListener listener() {
        return message -> {
            CacheOrderStatisticDto statistic;
            try {
                statistic = objectMapper.readValue(message.getBody(), CacheOrderStatisticDto.class);
            } catch (IOException ex) {
                log.error("Cannot parse order rates cache message", ex);
                return;
            }
            currencyPairService.refreshCurrencyPairRate(statistic);
        };
    }
}