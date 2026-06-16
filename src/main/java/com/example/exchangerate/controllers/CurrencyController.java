package com.example.exchangerate.controllers;

import com.example.exchangerate.models.CurrencyInfo;
import com.example.exchangerate.services.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public Collection<CurrencyInfo> getSupportedCurrencies() {
        return currencyService.getSupportedCurrencies();
    }

    @GetMapping("/{code}")
    public CurrencyInfo getCurrency(@PathVariable String code) {
        CurrencyInfo currency = currencyService.getCurrency(code);
        if (currency == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unsupported currency: " + code);
        }
        return currency;
    }
}
