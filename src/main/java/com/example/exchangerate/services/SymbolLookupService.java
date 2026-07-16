package com.example.exchangerate.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolLookupService {

    private final CurrencyService currencyService;

    public String getSymbol(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        var currency = currencyService.getCurrency(code);
        return currency != null ? currency.getSymbol() : null;
    }

    public Map<String, String> getAllSymbols() {
        Map<String, String> symbols = new LinkedHashMap<>();
        currencyService.getSupportedCurrencies().forEach(c ->
                symbols.put(c.getCode(), c.getSymbol()));
        return symbols;
    }

    public boolean isKnownSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return false;
        }
        return currencyService.getSupportedCurrencies().stream()
                .anyMatch(c -> symbol.equals(c.getSymbol()));
    }
}
