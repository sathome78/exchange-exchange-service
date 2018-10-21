package me.exrates.exchange.repositories;

import me.exrates.exchange.entities.CoinmarketcupDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CoinmarketcupDictionaryRepository extends JpaRepository<CoinmarketcupDictionary, Long> {

    Set<CoinmarketcupDictionary> findAllByEnabledTrue();
}
