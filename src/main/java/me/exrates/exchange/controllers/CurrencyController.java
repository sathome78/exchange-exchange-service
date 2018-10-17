package me.exrates.exchange.controllers;

import me.exrates.exchange.exceptions.ValidationException;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/finance")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PutMapping(value = "/rate/btc", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pair<String, BigDecimal>> getBTCRate(@RequestParam(value = "currency_name", defaultValue = "UAH") String currencyName) {
        return ResponseEntity.ok(Pair.of(currencyName, currencyService.getBTCRateForCurrency(currencyName)));
    }

    @PutMapping(value = "/rate/usd", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Pair<String, BigDecimal>> getUSDRate(@RequestParam(value = "currency_name", defaultValue = "UAH") String currencyName) {
        return ResponseEntity.ok(Pair.of(currencyName, currencyService.getUSDRateForCurrency(currencyName)));
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
}
