package me.exrates.exchange.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.exchange.converters.LocalDateDeserializer;
import me.exrates.exchange.converters.LocalDateSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyHistoryDto {

    private Long id;
    @JsonProperty("usd_rate")
    private BigDecimal usdRate;
    @JsonProperty("btc_rate")
    private BigDecimal btcRate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonProperty("created_at")
    private LocalDate createdAt;
    private CurrencyDto currency;
}