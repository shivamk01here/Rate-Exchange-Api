package com.example.exchangerate.calculator;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorService {

    private final CalculatorHistoryRepository historyRepository;
    private final ExchangeRateOrchestrationService orchestrationService;

    private static final int SCALE = 4;

    public CompletableFuture<CalculatorHistory> calculate(String fromCurrency, String toCurrency, BigDecimal amount) {
        ExchangeRateRequest request = ExchangeRateRequest.builder()
                .fromCurrency(fromCurrency.toUpperCase())
                .toCurrency(toCurrency.toUpperCase())
                .amount(amount)
                .build();

        return orchestrationService.getRate(request)
                .thenApply(response -> {
                    CalculatorHistory history = CalculatorHistory.builder()
                            .fromCurrency(fromCurrency.toUpperCase())
                            .toCurrency(toCurrency.toUpperCase())
                            .amount(amount)
                            .rate(response.getRate())
                            .convertedAmount(response.getConvertedAmount())
                            .provider(response.getProviderCode() != null ? response.getProviderCode().name() : null)
                            .favorite(false)
                            .calculatedAt(Instant.now())
                            .build();
                    CalculatorHistory saved = historyRepository.save(history);
                    log.info("Calculation performed: {}->{} amount={} rate={} result={}",
                            fromCurrency, toCurrency, amount, response.getRate(), response.getConvertedAmount());
                    return saved;
                });
    }

    public CompletableFuture<CalculatorHistory> recalculate(String id) {
        return historyRepository.findById(id)
                .map(existing -> calculate(existing.getFromCurrency(), existing.getToCurrency(), existing.getAmount())
                        .thenApply(newHistory -> {
                            CalculatorHistory updated = CalculatorHistory.builder()
                                    .id(existing.getId())
                                    .fromCurrency(newHistory.getFromCurrency())
                                    .toCurrency(newHistory.getToCurrency())
                                    .amount(newHistory.getAmount())
                                    .rate(newHistory.getRate())
                                    .convertedAmount(newHistory.getConvertedAmount())
                                    .provider(newHistory.getProvider())
                                    .favorite(existing.isFavorite())
                                    .calculatedAt(Instant.now())
                                    .build();
                            return historyRepository.save(updated);
                        }))
                .orElseThrow(() -> new IllegalArgumentException("History entry not found: " + id));
    }

    public Optional<CalculatorHistory> getHistory(String id) {
        return historyRepository.findById(id);
    }

    public List<CalculatorHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    public List<CalculatorHistory> getHistoryByPair(String from, String to) {
        return historyRepository.findByCurrencyPair(from, to);
    }

    public List<CalculatorHistory> getFavorites() {
        return historyRepository.findFavorites();
    }

    public CalculatorHistory toggleFavorite(String id) {
        return historyRepository.findById(id)
                .map(existing -> {
                    CalculatorHistory updated = CalculatorHistory.builder()
                            .id(existing.getId())
                            .fromCurrency(existing.getFromCurrency())
                            .toCurrency(existing.getToCurrency())
                            .amount(existing.getAmount())
                            .rate(existing.getRate())
                            .convertedAmount(existing.getConvertedAmount())
                            .provider(existing.getProvider())
                            .favorite(!existing.isFavorite())
                            .calculatedAt(existing.getCalculatedAt())
                            .build();
                    CalculatorHistory saved = historyRepository.save(updated);
                    log.info("Calculator history favorite toggled: id={} favorite={}", id, saved.isFavorite());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("History entry not found: " + id));
    }

    public boolean deleteHistory(String id) {
        boolean deleted = historyRepository.deleteById(id);
        if (deleted) {
            log.info("Calculator history deleted: id={}", id);
        }
        return deleted;
    }

    public void clearHistory() {
        historyRepository.deleteAll();
        log.info("All calculator history cleared");
    }

    public long getHistoryCount() {
        return historyRepository.count();
    }

    public CalculatorHistory reverse(String id) {
        return historyRepository.findById(id)
                .map(existing -> {
                    if (existing.getConvertedAmount() == null || existing.getRate() == null) {
                        throw new IllegalArgumentException("Cannot reverse: missing rate or converted amount");
                    }
                    BigDecimal reverseRate = BigDecimal.ONE.divide(existing.getRate(), SCALE, RoundingMode.HALF_UP);
                    CalculatorHistory reversed = CalculatorHistory.builder()
                            .fromCurrency(existing.getToCurrency())
                            .toCurrency(existing.getFromCurrency())
                            .amount(existing.getAmount())
                            .rate(reverseRate)
                            .convertedAmount(existing.getConvertedAmount())
                            .provider(existing.getProvider())
                            .favorite(false)
                            .calculatedAt(Instant.now())
                            .build();
                    CalculatorHistory saved = historyRepository.save(reversed);
                    log.info("Calculation reversed: id={} {}->{} {} -> {}",
                            id, existing.getToCurrency(), existing.getFromCurrency(),
                            existing.getConvertedAmount(), existing.getAmount());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("History entry not found: " + id));
    }
}
