package com.example.exchangerate.controllers;

import com.example.exchangerate.calculator.CalculatorHistory;
import com.example.exchangerate.calculator.CalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/calculator")
@RequiredArgsConstructor
public class CalculatorController {

    private final CalculatorService calculatorService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<CalculatorHistory> calculate(@Valid @RequestBody CalculatorHistory request) {
        if (request.getFromCurrency() == null || request.getToCurrency() == null || request.getAmount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromCurrency, toCurrency, and amount are required");
        }
        log.info("Calculator request: {}->{} amount={}", request.getFromCurrency(), request.getToCurrency(), request.getAmount());
        return calculatorService.calculate(request.getFromCurrency(), request.getToCurrency(), request.getAmount());
    }

    @GetMapping
    public List<CalculatorHistory> getAllHistory() {
        return calculatorService.getAllHistory();
    }

    @GetMapping("/{id}")
    public CalculatorHistory getHistory(@PathVariable String id) {
        return calculatorService.getHistory(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "History entry not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<CalculatorHistory> getHistoryByPair(@RequestParam String from, @RequestParam String to) {
        return calculatorService.getHistoryByPair(from, to);
    }

    @GetMapping("/favorites")
    public List<CalculatorHistory> getFavorites() {
        return calculatorService.getFavorites();
    }

    @PatchMapping("/{id}/favorite")
    public CalculatorHistory toggleFavorite(@PathVariable String id) {
        try {
            return calculatorService.toggleFavorite(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{id}/reverse")
    public CalculatorHistory reverse(@PathVariable String id) {
        try {
            return calculatorService.reverse(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{id}/recalculate")
    public CompletableFuture<CalculatorHistory> recalculate(@PathVariable String id) {
        try {
            return calculatorService.recalculate(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteHistory(@PathVariable String id) {
        boolean deleted = calculatorService.deleteHistory(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "History entry not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @DeleteMapping
    public Map<String, String> clearHistory() {
        calculatorService.clearHistory();
        return Map.of("status", "cleared");
    }

    @GetMapping("/count")
    public Map<String, Object> getHistoryCount() {
        return Map.of("count", calculatorService.getHistoryCount());
    }
}
