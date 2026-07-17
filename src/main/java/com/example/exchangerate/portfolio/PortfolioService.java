package com.example.exchangerate.portfolio;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final ExchangeRateOrchestrationService orchestrationService;

    public CurrencyPortfolio createPortfolio(CurrencyPortfolio portfolio) {
        validatePortfolio(portfolio);
        CurrencyPortfolio saved = portfolioRepository.save(portfolio);
        log.info("Portfolio created: id={} name={} baseCurrency={}",
                saved.getId(), saved.getName(), saved.getBaseCurrency());
        return saved;
    }

    private void validatePortfolio(CurrencyPortfolio portfolio) {
        if (portfolio.getHoldings() != null) {
            for (Map.Entry<String, BigDecimal> entry : portfolio.getHoldings().entrySet()) {
                if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Holding amount for " + entry.getKey() + " must be positive");
                }
            }
        }
    }

    public Optional<CurrencyPortfolio> getPortfolio(String id) {
        return portfolioRepository.findById(id);
    }

    public List<CurrencyPortfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

    public boolean deletePortfolio(String id) {
        boolean deleted = portfolioRepository.deleteById(id);
        if (deleted) {
            log.info("Portfolio deleted: id={}", id);
        }
        return deleted;
    }

    public CurrencyPortfolio updatePortfolio(String id, CurrencyPortfolio updated) {
        if (updated.getHoldings() != null) {
            validatePortfolio(updated);
        }
        return portfolioRepository.findById(id)
                .map(existing -> {
                    CurrencyPortfolio merged = CurrencyPortfolio.builder()
                            .id(existing.getId())
                            .name(updated.getName() != null ? updated.getName() : existing.getName())
                            .baseCurrency(updated.getBaseCurrency() != null ? updated.getBaseCurrency() : existing.getBaseCurrency())
                            .holdings(updated.getHoldings() != null ? updated.getHoldings() : existing.getHoldings())
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyPortfolio saved = portfolioRepository.save(merged);
                    log.info("Portfolio updated: id={} name={}", id, saved.getName());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + id));
    }

    public CurrencyPortfolio addHolding(String id, String currency, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Holding amount must be positive");
        }
        return portfolioRepository.findById(id)
                .map(existing -> {
                    Map<String, BigDecimal> holdings = new LinkedHashMap<>(existing.getHoldings());
                    holdings.merge(currency, amount, BigDecimal::add);
                    CurrencyPortfolio merged = CurrencyPortfolio.builder()
                            .id(existing.getId())
                            .name(existing.getName())
                            .baseCurrency(existing.getBaseCurrency())
                            .holdings(holdings)
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyPortfolio saved = portfolioRepository.save(merged);
                    log.info("Holding added: portfolioId={} {}={}", id, currency, amount);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + id));
    }

    public CurrencyPortfolio removeHolding(String id, String currency) {
        return portfolioRepository.findById(id)
                .map(existing -> {
                    Map<String, BigDecimal> holdings = new LinkedHashMap<>(existing.getHoldings());
                    if (!holdings.containsKey(currency)) {
                        throw new IllegalArgumentException("Currency not found in portfolio: " + currency);
                    }
                    holdings.remove(currency);
                    CurrencyPortfolio merged = CurrencyPortfolio.builder()
                            .id(existing.getId())
                            .name(existing.getName())
                            .baseCurrency(existing.getBaseCurrency())
                            .holdings(holdings)
                            .createdAt(existing.getCreatedAt())
                            .build();
                    CurrencyPortfolio saved = portfolioRepository.save(merged);
                    log.info("Holding removed: portfolioId={} currency={}", id, currency);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + id));
    }

    public long getPortfolioCount() {
        return portfolioRepository.count();
    }

    public CompletableFuture<PortfolioValuation> valuatePortfolio(String id) {
        return portfolioRepository.findById(id)
                .map(portfolio -> {
                    String baseCurrency = portfolio.getBaseCurrency();
                    Map<String, BigDecimal> holdings = portfolio.getHoldings();

                    if (holdings == null || holdings.isEmpty()) {
                        PortfolioValuation valuation = PortfolioValuation.builder()
                                .portfolioId(id)
                                .portfolioName(portfolio.getName())
                                .baseCurrency(baseCurrency)
                                .holdings(holdings)
                                .holdingValues(new LinkedHashMap<>())
                                .totalValue(BigDecimal.ZERO)
                                .status("EMPTY")
                                .valuedAt(Instant.now())
                                .build();
                        return CompletableFuture.completedFuture(valuation);
                    }

                    List<CompletableFuture<PortfolioValuation.HoldingValue>> futures = holdings.entrySet().stream()
                            .map(entry -> {
                                String currency = entry.getKey();
                                BigDecimal amount = entry.getValue();

                                if (currency.equalsIgnoreCase(baseCurrency)) {
                                    PortfolioValuation.HoldingValue hv = PortfolioValuation.HoldingValue.builder()
                                            .currency(currency)
                                            .amount(amount)
                                            .rate(BigDecimal.ONE)
                                            .convertedValue(amount)
                                            .status("SAME_CURRENCY")
                                            .build();
                                    return CompletableFuture.completedFuture(hv);
                                }

                                ExchangeRateRequest request = ExchangeRateRequest.builder()
                                        .fromCurrency(currency)
                                        .toCurrency(baseCurrency)
                                        .amount(amount)
                                        .build();

                                return orchestrationService.getRate(request)
                                        .thenApply(response -> PortfolioValuation.HoldingValue.builder()
                                                .currency(currency)
                                                .amount(amount)
                                                .rate(response.getRate())
                                                .convertedValue(response.getConvertedAmount())
                                                .status(response.getStatus())
                                                .build())
                                        .exceptionally(e -> {
                                            log.warn("Failed to get rate for {}: {}", currency, e.getMessage());
                                            return PortfolioValuation.HoldingValue.builder()
                                                    .currency(currency)
                                                    .amount(amount)
                                                    .status("FAILED")
                                                    .build();
                                        });
                            })
                            .collect(Collectors.toList());

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                Map<String, PortfolioValuation.HoldingValue> holdingValues = new LinkedHashMap<>();
                                BigDecimal totalValue = BigDecimal.ZERO;
                                boolean allFailed = true;

                                for (CompletableFuture<PortfolioValuation.HoldingValue> future : futures) {
                                    PortfolioValuation.HoldingValue hv = future.join();
                                    holdingValues.put(hv.getCurrency(), hv);
                                    if (hv.getConvertedValue() != null) {
                                        totalValue = totalValue.add(hv.getConvertedValue());
                                        allFailed = false;
                                    }
                                }

                                return PortfolioValuation.builder()
                                        .portfolioId(id)
                                        .portfolioName(portfolio.getName())
                                        .baseCurrency(baseCurrency)
                                        .holdings(holdings)
                                        .holdingValues(holdingValues)
                                        .totalValue(totalValue.setScale(4, RoundingMode.HALF_UP))
                                        .status(allFailed ? "FAILED" : "SUCCESS")
                                        .valuedAt(Instant.now())
                                        .build();
                            });
                })
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + id));
    }
}
