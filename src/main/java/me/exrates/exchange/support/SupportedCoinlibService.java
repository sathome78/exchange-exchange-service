package me.exrates.exchange.support;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.exrates.exchange.utils.CsvReaderUtil;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class SupportedCoinlibService {

    private static final String CSV_FILE = "coinlib.csv";

    private final Map<String, String> codes;

    public SupportedCoinlibService() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CSV_FILE);

        log.debug("Reading Coinlib info CSV");
        Set<CoinInfo> info = CsvReaderUtil.readAndMap(
                inputStream,
                line -> CoinInfo.builder()
                        .symbol(line[0])
                        .searchId(line[1])
                        .name(line[2])
                        .enabled(Boolean.valueOf(line[3]))
                        .build());

        this.codes = info.stream()
                .filter(CoinInfo::getEnabled)
                .filter(coinInfo -> nonNull(coinInfo.getSymbol()))
                .collect(toMap(
                        CoinInfo::getSymbol,
                        CoinInfo::getSearchId,
                        (k1, k2) -> {
                            log.debug("Duplicate key: {}", k2);
                            return k2;
                        }));

    }

    public String getSearchId(String symbol) {
        Objects.requireNonNull(symbol, "Requested symbol can't be null");
        return codes.get(symbol);
    }

    @Builder
    @Getter
    @EqualsAndHashCode(of = {"symbol", "searchId", "name"})
    @ToString
    private static class CoinInfo {
        @NonNull
        private String symbol;
        @NonNull
        private String searchId;
        @NonNull
        private String name;
        @NonNull
        private Boolean enabled;
    }
}
