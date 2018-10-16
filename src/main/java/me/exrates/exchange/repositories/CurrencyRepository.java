package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Query("SELECT ce.type FROM CurrencyExchange ce WHERE ce.name = :currencyName")
    ExchangerType getType(String currencyName);

    @Query("SELECT ce.btcRate FROM CurrencyExchange ce WHERE ce.name = :currencyName")
    BigDecimal getBtcRate(String currencyName);

    @Query("SELECT ce.usdRate FROM CurrencyExchange ce WHERE ce.name = :currencyName")
    BigDecimal getUsdRate(String currencyName);

    @Query("UPDATE CurrencyExchange ce SET ce.btcRate = :btcRate, ce.btcRateUpdatedAt = CURRENT_TIMESTAMP WHERE ce.name = :currencyName")
    void updateBtcRate(String currencyName, BigDecimal btcRate);

    @Query("UPDATE CurrencyExchange ce SET ce.usdRate = :usdRate, ce.usdRateUpdatedAt = CURRENT_TIMESTAMP WHERE ce.name = :currencyName")
    void updateUsdRate(String currencyName, BigDecimal usdRate);
}
