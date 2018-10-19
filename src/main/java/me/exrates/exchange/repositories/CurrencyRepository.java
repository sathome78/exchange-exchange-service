package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Transactional(readOnly = true)
    Currency getByName(String currencySymbol);

    @Transactional
    @Modifying
    @Query("UPDATE Currency c SET c.btcRate = :btc_rate, c.btcRateUpdatedAt = CURRENT_TIMESTAMP, c.usdRate = :usd_rate, c.usdRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :symbol")
    void updateRates(@Param("symbol") String currencySymbol, @Param("btc_rate") BigDecimal btcRate, @Param("usd_rate") BigDecimal usdRate);

    @Transactional
    @Modifying
    @Query("UPDATE Currency c SET c.type = :type, c.usdRate = 0, c.usdRateUpdatedAt = CURRENT_TIMESTAMP, c.btcRate = 0, c.btcRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :symbol")
    void updateExchangerType(@Param("symbol") String currencySymbol, @Param("type") ExchangerType newType);
}
