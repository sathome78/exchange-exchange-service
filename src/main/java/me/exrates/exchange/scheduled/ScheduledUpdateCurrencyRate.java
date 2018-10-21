package me.exrates.exchange.scheduled;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class ScheduledUpdateCurrencyRate {
    private final CurrencyService currencyService;

    @Autowired
    public ScheduledUpdateCurrencyRate(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(cron = "${scheduled.update.currency}")
    public void updateCurrencyRate() {
        currencyService.refreshCurrencyRate();
    }
}
