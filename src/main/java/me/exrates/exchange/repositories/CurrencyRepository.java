package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Query("SELECT c.type FROM Currency c WHERE c.name = :symbol")
    ExchangerType getType(@Param("symbol") String currencySymbol);

    @Query("SELECT c.btcRate FROM Currency c WHERE c.name = :symbol")
    BigDecimal getBtcRate(@Param("symbol") String currencySymbol);

    @Query("SELECT c.usdRate FROM Currency c WHERE c.name = :symbol")
    BigDecimal getUsdRate(@Param("symbol") String currencySymbol);

    @Modifying
    @Query("UPDATE Currency c SET c.btcRate = :rate, c.btcRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :symbol")
    void updateBtcRate(@Param("symbol") String currencySymbol, @Param("rate") BigDecimal btcRate);

    @Modifying
    @Query("UPDATE Currency c SET c.usdRate = :rate, c.usdRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :symbol")
    void updateUsdRate(@Param("symbol") String currencySymbol, @Param("rate") BigDecimal usdRate);

    @Modifying
    @Query("UPDATE Currency c SET c.type = :type, c.usdRate = 0, c.usdRateUpdatedAt = CURRENT_TIMESTAMP, c.btcRate = 0, c.btcRateUpdatedAt = CURRENT_TIMESTAMP WHERE c.name = :symbol")
    void updateExchangerType(@Param("symbol") String currencySymbol, @Param("type") ExchangerType newType);
}
