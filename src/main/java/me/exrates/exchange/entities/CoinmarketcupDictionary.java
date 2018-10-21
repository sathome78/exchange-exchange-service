package me.exrates.exchange.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
@Indexed
@Entity
@Table(name = "coinmarketcup_dictionary")
public class CoinmarketcupDictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    @Column(name = "currency_symbol", nullable = false, length = 16)
    private String currencySymbol;

    @Column(name = "coinmarketcup_currency_symbol", nullable = false, length = 16)
    private String coinmarketcupSymbol;

    @Column(name = "coinmarketcup_currency_name", length = 32)
    private String coinmarketcupName;

    @Column(name = "enabled")
    private boolean enabled;
}
