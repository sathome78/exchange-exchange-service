package me.exrates.exchange.components;

import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.exceptions.NoSuchExchangerException;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.function.Function.identity;

@Slf4j
@Component
public class ExchangerFactoryImpl implements ExchangerFactory {

    private final Map<ExchangerType, Exchanger> exchangers;

    @Autowired
    public ExchangerFactoryImpl(List<Exchanger> exchangers) {
        this.exchangers = exchangers.stream().collect(Collectors.toMap(Exchanger::getExchangerType, identity()));
    }

    @Override
    public Set<ExchangerType> getAvailableExchangers() {
        return exchangers.keySet();
    }

    @Override
    public Exchanger getExchanger(ExchangerType type) {
        Exchanger exchanger = exchangers.get(type);
        if (isNull(exchanger)) {
            throw new NoSuchExchangerException(String.format("No exchanger found with name: %s", type));
        }
        return exchanger;
    }
}
