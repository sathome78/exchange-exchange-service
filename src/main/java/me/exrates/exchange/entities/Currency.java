package me.exrates.exchange.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.exrates.exchange.models.enums.ExchangerType;
import org.springframework.stereotype.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"symbol", "exchangerType", "exchangerSymbol"})
@Indexed
@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @Column(name = "symbol", unique = true, nullable = false, length = 16)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchanger_type", nullable = false)
    private ExchangerType exchangerType;

    @Column(name = "exchanger_symbol", nullable = false, length = 32)
    private String exchangerSymbol;

    @Column(name = "usd_rate")
    private BigDecimal usdRate;

    @Column(name = "usd_rate_updated_at")
    private LocalDateTime usdRateUpdatedAt;

    @Column(name = "btc_rate")
    private BigDecimal btcRate;

    @Column(name = "btc_rate_updated_at")
    private LocalDateTime btcRateUpdatedAt;
}
