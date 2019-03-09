package me.exrates.exchange.cache;

import me.exrates.exchange.models.dto.CacheOrderStatisticDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class ExchangeRatesRedisRepository {

    private static final String key = "exchange_rates_holder";

    private final HashOperations<String, Object, Object> ops;

    @Autowired
    public ExchangeRatesRedisRepository(RedisTemplate<String, Object> redisTemplate) {
        redisTemplate.setHashKeySerializer(new GenericToStringSerializer<>(Integer.class));
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(CacheOrderStatisticDto.class));
        ops = redisTemplate.opsForHash();
    }

    public void put(CacheOrderStatisticDto statistic) {
        ops.put(key, statistic.getCurrencyPairName(), statistic);
    }

    public CacheOrderStatisticDto get(String currencyPairName) {
        return (CacheOrderStatisticDto) ops.get(key, currencyPairName);
    }

    public boolean exist(Integer currencyPairId) {
        return ops.hasKey(key, currencyPairId);
    }

    public List<CacheOrderStatisticDto> getAll() {
        return ops.values(key).stream()
                .map(o -> (CacheOrderStatisticDto) o)
                .collect(toList());
    }

    public List<CacheOrderStatisticDto> getByListId(List<Integer> ids) {
        return ops.multiGet(key, Collections.unmodifiableCollection(ids))
                .stream()
                .map(o -> (CacheOrderStatisticDto) o)
                .collect(toList());
    }

    public void delete(String currencyPairName) {
        ops.delete(key, currencyPairName);
    }

    @Transactional
    public void update(CacheOrderStatisticDto exOrderStatisticsShortByPairsDto) {
        delete(exOrderStatisticsShortByPairsDto.getCurrencyPairName());
        put(exOrderStatisticsShortByPairsDto);
    }

    @Transactional
    public void batchUpdate(List<CacheOrderStatisticDto> statisticList) {
        statisticList.forEach(this::update);
    }
}