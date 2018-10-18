package me.exrates.exchange.api;

import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(
        value = "exchange-client",
        configuration = {FeignConfiguration.class})
public interface ExchangeApi {

    @PostMapping("/btc-rate")
    Map<String, BigDecimal> getBTCRate(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol);

    @PostMapping("/usd-rate")
    Map<String, BigDecimal> getUSDRate(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol);

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    CurrencyDto createCurrency(@Validated @RequestBody CurrencyForm form);

    @DeleteMapping("/delete")
    void deleteCurrency(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol);

    @PostMapping("/update-type")
    void updateCurrencyType(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol,
                            @RequestParam(value = "exchanger_type", defaultValue = "FREE_CURRENCY") ExchangerType type);
}
