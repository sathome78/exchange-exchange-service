package me.exrates.exchange.configurations;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {

    public static final String CACHE_COIN_MARKET_CUP_EXCHANGER = "cache.coinmarketcup.exchanger";
    public static final String CACHE_WORLD_COIN_INDEX_EXCHANGER = "cache.worldcoinindex.exchanger";
    public static final String CACHE_FREE_CURRENCY_EXCHANGER = "cache.freecurrency.exchanger";
    public static final String CACHE_EXRATES_EXCHANGER = "cache.exrates.exchanger";
    public static final String CACHE_COINLIB_EXCHANGER = "cache.coinlib.exchanger";

    @Bean
    @Qualifier(CACHE_COIN_MARKET_CUP_EXCHANGER)
    public Cache cacheCoinMarketCup() {
        return new CaffeineCache(CACHE_COIN_MARKET_CUP_EXCHANGER, Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build());
    }

    @Bean
    @Qualifier(CACHE_WORLD_COIN_INDEX_EXCHANGER)
    public Cache cacheWorldCoinIndex() {
        return new CaffeineCache(CACHE_WORLD_COIN_INDEX_EXCHANGER, Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build());
    }

    @Bean
    @Qualifier(CACHE_FREE_CURRENCY_EXCHANGER)
    public Cache cacheFreeCurrency() {
        return new CaffeineCache(CACHE_FREE_CURRENCY_EXCHANGER, Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build());
    }

    @Bean
    @Qualifier(CACHE_EXRATES_EXCHANGER)
    public Cache cacheExrates() {
        return new CaffeineCache(CACHE_EXRATES_EXCHANGER, Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build());
    }

    @Bean
    @Qualifier(CACHE_COINLIB_EXCHANGER)
    public Cache cacheCoinlib() {
        return new CaffeineCache(CACHE_COINLIB_EXCHANGER, Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build());
    }
}
