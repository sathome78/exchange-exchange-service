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

    public static final String CACHE_COIN_MARKET_CUP_CODES = "cache.coinmarketcup.exchanger";
    public static final String CACHE_COINLIB_CODES = "cache.coinlib.exchanger";

    @Bean
    @Qualifier(CACHE_COIN_MARKET_CUP_CODES)
    public Cache cacheCoinMarketCup() {
        return new CaffeineCache(CACHE_COIN_MARKET_CUP_CODES, Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build());
    }

    @Bean
    @Qualifier(CACHE_COINLIB_CODES)
    public Cache cacheCoinlib() {
        return new CaffeineCache(CACHE_COINLIB_CODES, Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build());
    }
}
