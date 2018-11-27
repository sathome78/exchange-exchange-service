package me.exrates.exchange.scheduled;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.services.CurrencyHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class ScheduledCurrencyRatesDailyHistory {

    private final CurrencyHistoryService currencyHistoryService;

    @Autowired
    public ScheduledCurrencyRatesDailyHistory(CurrencyHistoryService currencyHistoryService) {
        this.currencyHistoryService = currencyHistoryService;
    }

    @Scheduled(cron = "${scheduled.update.history}")
    public void updateCurrencyRateHistory() {
        currencyHistoryService.updateCurrencyRateHistory();
    }
}