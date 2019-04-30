package me.exrates.exchange.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.exchange.converters.LocalDateTimeDeserializer;
import me.exrates.exchange.converters.LocalDateTimeSerializer;
import me.exrates.exchange.models.enums.ExchangerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDto {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String symbol;
    @JsonProperty("exchanger_type")
    private ExchangerType exchangerType;
    @JsonProperty("exchanger_symbol")
    private String exchangerSymbol;
    @JsonProperty("usd_rate")
    private BigDecimal usdRate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("usd_rate_updated_at")
    private LocalDateTime usdRateUpdatedAt;
    @JsonProperty("btc_rate")
    private BigDecimal btcRate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("btc_rate_updated_at")
    private LocalDateTime btcRateUpdatedAt;
    private String image;

    public static String getFullTitle() {
        return Stream.of("name", "exchanger_type", "exchanger_symbol", "usd_rate", "usd_rate_updated_at", "btc_rate", "btc_rate_updated_at")
                .collect(Collectors.joining(";", "", "\r\n"));
    }

    public String toString() {
        return Stream.of(
                String.valueOf(this.symbol),
                String.valueOf(this.exchangerType),
                String.valueOf(this.exchangerSymbol),
                String.valueOf(this.usdRate.toPlainString()),
                String.valueOf(this.usdRateUpdatedAt.format(FORMATTER)),
                String.valueOf(this.btcRate.toPlainString()),
                String.valueOf(this.btcRateUpdatedAt.format(FORMATTER))
        ).collect(Collectors.joining(";", "", "\r\n"));
    }
}
