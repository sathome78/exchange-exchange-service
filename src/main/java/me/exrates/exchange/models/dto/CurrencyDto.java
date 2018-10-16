package me.exrates.exchange.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private BigDecimal usdRate;
    private LocalDateTime usdRateUpdatedAt;
    private BigDecimal btcRate;
    private LocalDateTime btcRateUpdatedAt;
}
