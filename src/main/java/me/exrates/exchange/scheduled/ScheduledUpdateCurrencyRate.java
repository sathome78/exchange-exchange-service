package me.exrates.exchange.scheduled;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.services.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@EnableScheduling
public class ScheduledUpdateCurrencyRate {
    private final CurrencyService currencyService;

    @Autowired
    public ScheduledUpdateCurrencyRate(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

//    @PostConstruct
//    public void init() {
//        currencyService.refreshCurrencyRate();
//    }

    @Scheduled(cron = "${scheduled.update.currency}")
    public void updateCurrencyRate() {
        currencyService.refreshCurrencyRate();
    }
}
