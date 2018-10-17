package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Query("SELECT c.type FROM Currency c WHERE c.name = :currencyName")
    ExchangerType getType(String currencyName);

    @Query("SELECT c.btcRate FROM Currency c WHERE c.name = :currencyName")
    BigDecimal getBtcRate(String currencyName);

    @Query("SELECT c.usdRate FROM Currency c WHERE c.name = :currencyName")
    BigDecimal getUsdRate(String currencyName);

    @Query("UPDATE Currency c SET c.btcRate = :btcRate, c.btcRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :currencyName")
    void updateBtcRate(String currencyName, BigDecimal btcRate);

    @Query("UPDATE Currency c SET c.usdRate = :usdRate, c.usdRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :currencyName")
    void updateUsdRate(String currencyName, BigDecimal usdRate);

    @Query("UPDATE Currency c SET c.type = :newType, c.usdRate = null, c.usdRateUpdatedAt = null, c.btcRate = null, c.btcRateUpdatedAt = null WHERE c.name = :currencyName")
    void updateExchangerType(String currencyName, ExchangerType newType);
}
