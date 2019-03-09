package me.exrates.exchange.configurations;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfiguration {

//    @Value("${redis.cache.host}")
//    private String host;
//    @Value("${redis.cache.port}")
//    private int port;
//
//    @Bean
//    JedisPool jedisPool() {
//        log.info("Redis init {}:{}", host, port);
//        return new JedisPool(new JedisPoolConfig(), host, port);
//    }

    @Value("${redis.cache.host}")
    private String host;
    @Value("${redis.cache.port}")
    private Integer port;

    @Bean
    public JedisPoolConfig poolConfig() {
        final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxTotal(30);
        return jedisPoolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisConnFactory() {
        JedisConnectionFactory jedisConnFactory = new JedisConnectionFactory(poolConfig());
        jedisConnFactory.setUsePool(true);
        jedisConnFactory.setHostName(host);
        jedisConnFactory.setDatabase(Protocol.DEFAULT_DATABASE);
        jedisConnFactory.setTimeout(3000);
        jedisConnFactory.setPort(port);
        return jedisConnFactory;
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
