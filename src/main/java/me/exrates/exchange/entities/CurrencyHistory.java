package me.exrates.exchange.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.stereotype.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "usdRate", "btcRate"})
@ToString(exclude = {"currency"})
@Indexed
@Entity
@Table(name = "currency_history")
public class CurrencyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "usd_rate")
    private BigDecimal usdRate;

    @Column(name = "btc_rate")
    private BigDecimal btcRate;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    public CurrencyHistory(final Currency currency) {
        this.usdRate = currency.getUsdRate();
        this.btcRate = currency.getBtcRate();
        this.createdAt = LocalDate.now();
        this.currency = currency;
    }
}
