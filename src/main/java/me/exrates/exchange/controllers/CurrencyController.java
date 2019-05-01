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
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Controller
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

    @ResponseBody
    @GetMapping(value = "/rates/{currency_symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyDto> getRatesByCurrencySymbol(@PathVariable(value = "currency_symbol") String symbol) {
        Currency currency = currencyService.getRatesByCurrencySymbol(symbol);
        return ResponseEntity.ok(modelMapper.map(currency, CurrencyDto.class));
    }

    @ResponseBody
    @GetMapping(value = "/rates/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, CurrencyDto>> getAllRates() {
        List<Currency> all = currencyService.getRatesForAll();
        List<CurrencyDto> result = modelMapper.map(all, new TypeToken<List<CurrencyDto>>() {
        }.getType());
        return ResponseEntity.ok(result.stream().collect(toMap(CurrencyDto::getSymbol, Function.identity())));
    }

    @ResponseBody
    @GetMapping(value = "/rates/type/{currency_type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, CurrencyDto>> getRatesByType(@PathVariable(value = "currency_type") String type) {
        List<Currency> allByType = currencyService.getRatesByCurrencyType(type);
        List<CurrencyDto> result = modelMapper.map(allByType, new TypeToken<List<CurrencyDto>>() {
        }.getType());
        return ResponseEntity.ok(result.stream().collect(toMap(CurrencyDto::getSymbol, Function.identity())));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyDto> createCurrency(@Validated @RequestBody CurrencyForm form,
                                                      Errors result) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getAllErrors());
        }
        return ResponseEntity.ok(modelMapper.map(currencyService.create(form), CurrencyDto.class));
    }

    @ResponseBody
    @DeleteMapping(value = "/delete")
    public ResponseEntity deleteCurrency(@RequestParam(value = "currency_symbol", defaultValue = "BTC") String symbol) {
        currencyService.delete(symbol);
        return new ResponseEntity(HttpStatus.OK);
    }

    @ResponseBody
    @PutMapping(value = "/update")
    public ResponseEntity updateCurrency(@RequestParam(value = "currency_symbol", defaultValue = "BTC") String symbol,
                                         @RequestParam(value = "exchanger_type", defaultValue = "COIN_MARKET_CUP") ExchangerType exchangerType,
                                         @RequestParam(value = "exchanger_symbol", defaultValue = "bitcoin") String exchangerSymbol,
                                         @RequestParam(value = "image") String image) {
        currencyService.updateCurrency(symbol, exchangerType, exchangerSymbol, image);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "/load", produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<String> loadCSV() {
        List<Currency> all = currencyService.getRatesForAll();
        List<CurrencyDto> result = modelMapper.map(all, new TypeToken<List<CurrencyDto>>() {
        }.getType());
        return ResponseEntity.ok(getRatesCSV(result));
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "index";
    }

    private String getRatesCSV(List<CurrencyDto> result) {
        return result.stream()
                .map(CurrencyDto::toString)
                .collect(Collectors.joining("", CurrencyDto.getFullTitle(), ""));
    }
}