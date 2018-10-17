package me.exrates.exchange.controllers;

import me.exrates.exchange.exceptions.ValidationException;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.services.CurrencyService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping(value = "/btc-rate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pair<String, BigDecimal>> getBTCRate(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol) {
        return ResponseEntity.ok(Pair.of(symbol, currencyService.getBTCRateForCurrency(symbol)));
    }

    @PostMapping(value = "/usd-rate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pair<String, BigDecimal>> getUSDRate(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol) {
        return ResponseEntity.ok(Pair.of(symbol, currencyService.getUSDRateForCurrency(symbol)));
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createCurrency(@Validated @RequestBody CurrencyForm form,
                                         Errors result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getAllErrors());
        }
        currencyService.create(form);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/update-type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateCurrencyType(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol,
                                             @RequestParam(value = "exchanger_type", defaultValue = "FREE_CURRENCY") ExchangerType type) {
        currencyService.updateExchangerType(symbol, type);
        return new ResponseEntity(HttpStatus.OK);
    }
}
