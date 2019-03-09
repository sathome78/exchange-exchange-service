package me.exrates.exchange.models.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CacheOrderStatisticDto {

    @JsonProperty("currency_pair_id")
    private Integer currencyPairId;
    @JsonProperty("currency_pair_name")
    private String currencyPairName;
    @JsonProperty("currency_pair_precision")
    private Integer currencyPairPrecision;
    @JsonProperty("last_order_rate")
    private BigDecimal lastOrderRate;
    @JsonProperty("pred_last_order_rate")
    private BigDecimal predLastOrderRate;
    @JsonProperty("percent_change")
    private BigDecimal percentChange;
    private String market;
    private BigDecimal currencyVolume;
    private BigDecimal volume;
    @JsonProperty("price_in_usd")
    private BigDecimal priceInUSD;
}
