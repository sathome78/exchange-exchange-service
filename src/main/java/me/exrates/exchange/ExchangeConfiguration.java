package me.exrates.exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.configurations.MongoConfiguration;
import me.exrates.exchange.configurations.ResourcesServerConfiguration;
import me.exrates.exchange.configurations.SwaggerConfiguration;
import me.exrates.exchange.configurations.WebSecurityConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = {"me.exrates.exchange.repositories"})
@EnableMongoRepositories(basePackages = {"me.exrates.exchange.repositories.mongodb"})
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan
@Import({
        WebSecurityConfiguration.class,
        ResourcesServerConfiguration.class,
        SwaggerConfiguration.class,
        MongoConfiguration.class
})
public class ExchangeConfiguration {

    public static final String JSON_MAPPER = "jsonMapper";

    @Bean(JSON_MAPPER)
    public ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
//        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
