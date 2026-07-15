package com.example.exchangerate.controllers;

import com.example.exchangerate.portfolio.CurrencyPortfolio;
import com.example.exchangerate.portfolio.HoldingRequest;
import com.example.exchangerate.portfolio.PortfolioService;
import com.example.exchangerate.portfolio.PortfolioValuation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CurrencyPortfolio createPortfolio(@Valid @RequestBody CurrencyPortfolio portfolio) {
        if (portfolio.getName() == null || portfolio.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (portfolio.getBaseCurrency() == null || portfolio.getBaseCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "baseCurrency is required");
        }
        log.info("Creating portfolio: name={} baseCurrency={}", portfolio.getName(), portfolio.getBaseCurrency());
        return portfolioService.createPortfolio(portfolio);
    }

    @GetMapping
    public List<CurrencyPortfolio> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    @GetMapping("/{id}")
    public CurrencyPortfolio getPortfolio(@PathVariable String id) {
        return portfolioService.getPortfolio(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found: " + id));
    }

    @PutMapping("/{id}")
    public CurrencyPortfolio updatePortfolio(@PathVariable String id, @Valid @RequestBody CurrencyPortfolio portfolio) {
        try {
            return portfolioService.updatePortfolio(id, portfolio);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deletePortfolio(@PathVariable String id) {
        boolean deleted = portfolioService.deletePortfolio(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PostMapping("/{id}/holdings")
    public CurrencyPortfolio addHolding(@PathVariable String id, @Valid @RequestBody HoldingRequest request) {
        try {
            return portfolioService.addHolding(id, request.getCurrency().toUpperCase(), request.getAmount());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}/holdings/{currency}")
    public CurrencyPortfolio removeHolding(@PathVariable String id, @PathVariable String currency) {
        try {
            return portfolioService.removeHolding(id, currency.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{id}/value")
    public CompletableFuture<PortfolioValuation> valuation(@PathVariable String id) {
        log.info("Valuing portfolio: id={}", id);
        try {
            return portfolioService.valuatePortfolio(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/count")
    public Map<String, Long> getPortfolioCount() {
        return Map.of("count", portfolioService.getPortfolioCount());
    }
}
