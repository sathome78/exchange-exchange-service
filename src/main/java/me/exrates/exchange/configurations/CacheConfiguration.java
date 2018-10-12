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

    @Bean
    @Qualifier(CACHE_COIN_MARKET_CUP_EXCHANGER)
    public Cache cacheCoinmarket() {
        return new CaffeineCache(CACHE_COIN_MARKET_CUP_EXCHANGER, Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());
    }
}
