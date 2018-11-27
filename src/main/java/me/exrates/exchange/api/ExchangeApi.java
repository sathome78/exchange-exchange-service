package me.exrates.exchange.api;

import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.dto.CurrencyHistoryDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        value = "exchange-client",
        configuration = {FeignConfiguration.class})
public interface ExchangeApi {

    @GetMapping("/rates/{currency_symbol}")
    CurrencyDto getRatesByCurrencySymbol(@PathVariable(value = "currency_symbol") String symbol);

    @GetMapping("/rates/all")
    Map<String, CurrencyDto> getAllRates();

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    CurrencyDto createCurrency(@Validated @RequestBody CurrencyForm form);

    @DeleteMapping("/delete")
    void deleteCurrency(@RequestParam(value = "currency_symbol", defaultValue = "BTC") String symbol);

    @PutMapping("/update")
    void updateCurrency(@RequestParam(value = "currency_symbol", defaultValue = "BTC") String symbol,
                        @RequestParam(value = "exchanger_type", defaultValue = "COIN_MARKET_CUP") ExchangerType exchangerType,
                        @RequestParam(value = "exchanger_symbol", defaultValue = "bitcoin") String exchangerSymbol);

    @GetMapping("/history/{currency_symbol}")
    List<CurrencyHistoryDto> getRatesHistoryByCurrencySymbol(@PathVariable(value = "currency_symbol") String symbol);
}
