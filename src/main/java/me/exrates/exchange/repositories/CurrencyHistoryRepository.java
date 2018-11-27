package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.CurrencyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CurrencyHistoryRepository extends JpaRepository<CurrencyHistory, Long> {

    @Transactional(readOnly = true)
    @Query("SELECT ch FROM CurrencyHistory ch JOIN ch.currency c WHERE c.symbol = ?1")
    List<CurrencyHistory> getAllByCurrencySymbol(String currencySymbol);
}