package me.exrates.exchange.services;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.entities.CurrencyHistory;
import me.exrates.exchange.repositories.CurrencyHistoryRepository;
import me.exrates.exchange.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static me.exrates.exchange.utils.CollectionUtil.isEmpty;

@Slf4j
@Service
public class CurrencyHistoryService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyHistoryRepository currencyHistoryRepository;

    @Autowired
    public CurrencyHistoryService(CurrencyRepository currencyRepository,
                                  CurrencyHistoryRepository currencyHistoryRepository) {
        this.currencyRepository = currencyRepository;
        this.currencyHistoryRepository = currencyHistoryRepository;
    }

    @Transactional
    public void updateCurrencyRateHistory() {
        List<Currency> all = currencyRepository.findAll();
        if (isEmpty(all)) {
            log.info("No currencies present in database");
            return;
        }

        all.forEach(currency -> currencyHistoryRepository.save(new CurrencyHistory(currency)));
    }

    @Transactional(readOnly = true)
    public List<CurrencyHistory> getAllByCurrencySymbol(String currencySymbol) {
        return currencyHistoryRepository.getAllByCurrencySymbol(currencySymbol);
    }
}