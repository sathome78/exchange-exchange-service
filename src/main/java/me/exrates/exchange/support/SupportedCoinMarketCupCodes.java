package me.exrates.exchange.support;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.utils.CsvReader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;

@Slf4j
@Getter
@Component
public class SupportedCoinMarketCupCodes {

    private static final String COINMARKETCUP_CSV_FILE = "coinmarketcup.csv";

    private final Set<CurrencyCode> currencyCodes;

    public SupportedCoinMarketCupCodes() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(COINMARKETCUP_CSV_FILE);

        log.debug("Reading coinmarketcup CSV");
        currencyCodes = CsvReader.readAndMap(
                is,
                line -> CurrencyCode.builder()
                        .symbol(line[0])
                        .searchId(line[1])
                        .build());
    }

    @Builder
    @Getter
    @EqualsAndHashCode(of = {"symbol", "searchId"})
    @ToString
    private static class CurrencyCode {
        @NonNull
        private String symbol;
        @NonNull
        private String searchId;
    }
}
