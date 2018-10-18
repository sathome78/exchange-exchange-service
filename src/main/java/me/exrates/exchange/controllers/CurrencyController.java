package me.exrates.exchange.controllers;

import me.exrates.exchange.exceptions.ValidationException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.dto.RateDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.services.CurrencyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    private final CurrencyService currencyService;
    private final ModelMapper modelMapper;

    @Autowired
    public CurrencyController(CurrencyService currencyService,
                              ModelMapper modelMapper) {
        this.currencyService = currencyService;
        this.modelMapper = modelMapper;
    }

    @PostMapping(value = "/btc-rate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RateDto> getBTCRate(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol) {
        final BigDecimal rate = currencyService.getBTCRateForCurrency(symbol);
        return ResponseEntity.ok(new RateDto(symbol, rate));
    }

    @PostMapping(value = "/btc-rate/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<ExchangerType, List<RateDto>>> getBTCRates() {
        return ResponseEntity.ok(currencyService.getBTCRateForAll());
    }

    @PostMapping(value = "/usd-rate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RateDto> getUSDRate(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol) {
        final BigDecimal rate = currencyService.getUSDRateForCurrency(symbol);
        return ResponseEntity.ok(new RateDto(symbol, rate));
    }

    @PostMapping(value = "/usd-rate/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<ExchangerType, List<RateDto>>> getUSDRates() {
        return ResponseEntity.ok(currencyService.getUSDRateForAll());
    }

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyDto> createCurrency(@Validated @RequestBody CurrencyForm form,
                                                      Errors result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getAllErrors());
        }
        return ResponseEntity.ok(modelMapper.map(currencyService.create(form), CurrencyDto.class));
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity deleteCurrency(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol) {
        currencyService.delete(symbol);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "/update-type")
    public ResponseEntity updateCurrencyType(@RequestParam(value = "currency_symbol", defaultValue = "UAH") String symbol,
                                             @RequestParam(value = "exchanger_type", defaultValue = "FREE_CURRENCY") ExchangerType type) {
        currencyService.updateExchangerType(symbol, type);
        return new ResponseEntity(HttpStatus.OK);
    }
}
