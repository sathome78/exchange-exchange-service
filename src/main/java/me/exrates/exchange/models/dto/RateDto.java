package me.exrates.exchange.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateDto {

    @JsonProperty("currency_symbol")
    private String currencySymbol;
    private BigDecimal value;
}
