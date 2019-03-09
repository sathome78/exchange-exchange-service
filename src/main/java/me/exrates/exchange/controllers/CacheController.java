package me.exrates.exchange.controllers;

import me.exrates.exchange.cache.ExchangeRatesRedisRepository;
import me.exrates.exchange.models.dto.CacheOrderStatisticDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final ExchangeRatesRedisRepository redisRepository;

    @Autowired
    public CacheController(ExchangeRatesRedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CacheOrderStatisticDto> getCacheByCurrencyPairSymbol(@RequestParam(value = "currency_pair_symbol") String symbol) {
        return ResponseEntity.ok(redisRepository.get(symbol));
    }

    @GetMapping(value = "/get/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CacheOrderStatisticDto>> getAllCache() {
        return ResponseEntity.ok(redisRepository.getAll());
    }
}
