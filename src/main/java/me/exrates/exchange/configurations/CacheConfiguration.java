package me.exrates.exchange.configurations;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    public final static String CACHE_COINMARKETCUP_ALL = "cache.coinmarketcap.all";

    @Bean(CACHE_COINMARKETCUP_ALL)
    public Cache cacheEthereumSearch() {
        return new CaffeineCache(CACHE_COINMARKETCUP_ALL, Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build());
    }
}
