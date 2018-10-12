package me.exrates.exchange.components;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.exceptions.NoSuchExchangerException;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;

@Slf4j
@Lazy
@Component
public class ExchangerFactoryImpl implements ExchangerFactory {

    private final Map<ExchangerType, Exchanger> exchangers;
    private final Cache<ExchangerType, Exchanger> exchangersCache;

    @Autowired
    public ExchangerFactoryImpl(List<Exchanger> exchangers,
                                @Value("${exchanger.refresh-rate:10}") int refreshRate) {
        this.exchangers = exchangers.stream().collect(Collectors.toMap(Exchanger::getExchangerType, identity()));
        this.exchangersCache = CacheBuilder.newBuilder()
                .expireAfterWrite(refreshRate, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Set<ExchangerType> getAvailableExchangers() {
        return exchangers.keySet();
    }

    @Override
    public Exchanger getExchanger(ExchangerType type) {
        try {
            return exchangersCache.get(type, () -> buildExchangerProcessing(type, exchangers.get(type)));
        } catch (ExecutionException e) {
            throw new NoSuchExchangerException(String.format("No exchanger found with name: %s", type));
        }
    }

    private Exchanger buildExchangerProcessing(ExchangerType type, Exchanger exchanger) {
        if (isNull(exchanger)) {
            throw new NoSuchExchangerException(String.format("No exchanger found with name: %s", type));
        }
        return exchanger;
    }
}
