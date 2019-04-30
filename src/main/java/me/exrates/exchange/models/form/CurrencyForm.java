package me.exrates.exchange.models.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.exrates.exchange.models.enums.ExchangerType;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyForm {

    @NotEmpty
    private String symbol;
    private ExchangerType exchangerType;
    private String exchangerSymbol;
    private String image;
}
