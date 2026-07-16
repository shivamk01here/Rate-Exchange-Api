package com.example.exchangerate.controllers;

import com.example.exchangerate.services.SymbolLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/symbol")
@RequiredArgsConstructor
public class SymbolLookupController {

    private final SymbolLookupService symbolLookupService;

    @GetMapping("/{code}")
    public Map<String, String> getSymbol(@PathVariable String code) {
        String symbol = symbolLookupService.getSymbol(code);
        if (symbol == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No symbol found for currency: " + code);
        }
        return Map.of("code", code.toUpperCase(), "symbol", symbol);
    }

    @GetMapping
    public Map<String, String> getAllSymbols() {
        return symbolLookupService.getAllSymbols();
    }

    @GetMapping("/check")
    public Map<String, Object> checkSymbol(@RequestParam String symbol) {
        boolean known = symbolLookupService.isKnownSymbol(symbol);
        return Map.of("symbol", symbol, "known", known);
    }
}
