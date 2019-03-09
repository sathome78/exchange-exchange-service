package me.exrates.exchange.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.models.dto.CacheOrderStatisticDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static me.exrates.exchange.ExchangeConfiguration.JSON_MAPPER;

@Slf4j
//@Service
public class RedisCurrencyPairRatesCache {

//    private final JedisPool jedisPool;
//    private final ObjectMapper objectMapper;
//
//    private int db;
//
//    @Autowired
//    public RedisCurrencyPairRatesCache(JedisPool jedisPool,
//                                       @Qualifier(JSON_MAPPER) ObjectMapper objectMapper,
//                                       @Value("${redis.cache.db}") int db) {
//        this.jedisPool = jedisPool;
//        this.objectMapper = objectMapper;
//        this.db = db;
//        log.info("RedisCurrencyPairRatesCache initialized on Redis db: {}", db);
//    }

//    /**
//     * Get Jedis instance
//     *
//     * @return {@link redis.clients.jedis.Jedis}
//     */
//    private Jedis getJedis() {
//        Jedis jedisPoolResource = jedisPool.getResource();
//        jedisPoolResource.select(db);
//        return jedisPoolResource;
//    }
//
//    public void clearDB() {
//        getJedis().flushDB();
//    }
//
//    public void save(List<CacheOrderStatisticDto> statisticList) {
//        if (isNull(statisticList) || statisticList.isEmpty()) {
//            return;
//        }
//        statisticList.forEach(this::save);
//    }
//
//    public void save(CacheOrderStatisticDto statistic) {
//        @Cleanup Jedis jedis = getJedis();
//
//        final String key = statistic.getCurrencyPairName();
//        String json;
//        try {
//            json = objectMapper.writeValueAsString(statistic);
//        } catch (JsonProcessingException ex) {
//            throw new RuntimeException(String.format("Could not serialize object, data not added to cache by key: %s", key), ex);
//        }
//        jedis.del(key);
//        jedis.set(key, json);
//    }
//
//    public CacheOrderStatisticDto get(String currencyPairName) {
//        try (Jedis jedis = getJedis()) {
//            if (jedis.exists(currencyPairName)) {
//                final String json = jedis.get(currencyPairName);
//
//                return nonNull(json) ? objectMapper.readValue(json, CacheOrderStatisticDto.class) : null;
//            }
//            log.info("Key: {} is not defined in cache", currencyPairName);
//            return null;
//        } catch (IOException ex) {
//            log.warn("Could not deserialize object");
//            return null;
//        }
//    }
//
//    public List<CacheOrderStatisticDto> getAll() {
//        Set<String> keys = getJedis().keys("*");
//
//        return keys.stream()
//                .map(this::get)
//                .filter(Objects::nonNull)
//                .collect(toList());
//    }
}
