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

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDto {

    private String name;
    private ExchangerType type;
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
}
