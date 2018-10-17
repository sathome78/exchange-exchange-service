package me.exrates.exchange.models.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.exchange.converters.LocalDateTimeDeserializer;
import me.exrates.exchange.converters.LocalDateTimeSerializer;
import me.exrates.exchange.models.enums.ExchangerType;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyDto {

    @NotEmpty
    private String name;
    @NotEmpty
    private ExchangerType type;
    private BigDecimal usdRate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime usdRateUpdatedAt;
    private BigDecimal btcRate;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime btcRateUpdatedAt;
}
