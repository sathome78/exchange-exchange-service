package me.exrates.exchange.controllers;

import me.exrates.exchange.entities.Currency;
import me.exrates.exchange.exceptions.ValidationException;
import me.exrates.exchange.models.dto.CurrencyDto;
import me.exrates.exchange.models.enums.ExchangerType;
import me.exrates.exchange.models.form.CurrencyForm;
import me.exrates.exchange.services.CurrencyService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

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

    @GetMapping(value = "/rates/{currency_symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyDto> getRatesByCurrencySymbol(@PathVariable(value = "currency_symbol") String symbol) {
        Currency currency = currencyService.getRatesByCurrencySymbol(symbol);
        return ResponseEntity.ok(modelMapper.map(currency, CurrencyDto.class));
    }

    @GetMapping(value = "/rates/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, CurrencyDto>> getAllRates() {
        List<Currency> all = currencyService.getRatesForAll();
        List<CurrencyDto> result = modelMapper.map(all, new TypeToken<List<CurrencyDto>>() {
        }.getType());
        return ResponseEntity.ok(result.stream().collect(toMap(CurrencyDto::getSymbol, Function.identity())));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyDto> createCurrency(@Validated @RequestBody CurrencyForm form,
                                                      Errors result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getAllErrors());
        }
        return ResponseEntity.ok(modelMapper.map(currencyService.create(form), CurrencyDto.class));
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity deleteCurrency(@RequestParam(value = "currency_symbol", defaultValue = "BTC") String symbol) {
        currencyService.delete(symbol);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping(value = "/update")
    public ResponseEntity updateCurrency(@RequestParam(value = "currency_symbol", defaultValue = "BTC") String symbol,
                                         @RequestParam(value = "exchanger_type", defaultValue = "COIN_MARKET_CUP") ExchangerType exchangerType,
                                         @RequestParam(value = "exchanger_symbol", defaultValue = "bitcoin") String exchangerSymbol) {
        currencyService.updateCurrency(symbol, exchangerType, exchangerSymbol);
        return new ResponseEntity(HttpStatus.OK);
    }
}
