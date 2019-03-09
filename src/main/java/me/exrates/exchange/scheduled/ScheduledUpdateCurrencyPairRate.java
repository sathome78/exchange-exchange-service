package me.exrates.exchange.scheduled;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.services.CurrencyPairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class ScheduledUpdateCurrencyPairRate {

    private final CurrencyPairService currencyPairService;

    @Autowired
    public ScheduledUpdateCurrencyPairRate(CurrencyPairService currencyPairService) {
        this.currencyPairService = currencyPairService;
    }

//    @Scheduled(cron = "${scheduled.update.currency-pair}")
//    public void updateCurrencyRate() {
//        currencyPairService.refreshAllCurrencyPairRates();
//    }
}
