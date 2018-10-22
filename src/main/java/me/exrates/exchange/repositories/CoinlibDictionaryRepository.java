package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.CoinlibDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CoinlibDictionaryRepository extends JpaRepository<CoinlibDictionary, Long> {

    CoinlibDictionary getByCurrencySymbolAndEnabledTrue(String currencySymbol);
}
