package com.example.exchangerate.calculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorSummaryService {

    private final CalculatorHistoryRepository historyRepository;

    public CalculatorSummary generateSummary() {
        List<CalculatorHistory> all = historyRepository.findAll();

        if (all.isEmpty()) {
            return CalculatorSummary.builder()
                    .totalConversions(0)
                    .favoriteCount(0)
                    .totalAmountConverted(BigDecimal.ZERO)
                    .averageRate(BigDecimal.ZERO)
                    .mostUsedPair(null)
                    .mostUsedProvider(null)
                    .pairFrequency(Collections.emptyMap())
                    .providerFrequency(Collections.emptyMap())
                    .uniqueCurrencies(Collections.emptyList())
                    .generatedAt(Instant.now())
                    .build();
        }

        long favoriteCount = all.stream().filter(CalculatorHistory::isFavorite).count();

        BigDecimal totalAmount = all.stream()
                .map(CalculatorHistory::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRate = all.stream()
                .map(CalculatorHistory::getRate)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long rateCount = all.stream().filter(h -> h.getRate() != null).count();
        BigDecimal averageRate = rateCount > 0
                ? totalRate.divide(BigDecimal.valueOf(rateCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Long> pairFrequency = all.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getFromCurrency() + "/" + h.getToCurrency(),
                        Collectors.counting()));
        String mostUsedPair = pairFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map<String, Long> providerFrequency = all.stream()
                .filter(h -> h.getProvider() != null)
                .collect(Collectors.groupingBy(
                        CalculatorHistory::getProvider,
                        Collectors.counting()));
        String mostUsedProvider = providerFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Set<String> currencies = new TreeSet<>();
        for (CalculatorHistory h : all) {
            if (h.getFromCurrency() != null) currencies.add(h.getFromCurrency());
            if (h.getToCurrency() != null) currencies.add(h.getToCurrency());
        }

        CalculatorSummary summary = CalculatorSummary.builder()
                .totalConversions(all.size())
                .favoriteCount(favoriteCount)
                .totalAmountConverted(totalAmount)
                .averageRate(averageRate)
                .mostUsedPair(mostUsedPair)
                .mostUsedProvider(mostUsedProvider)
                .pairFrequency(pairFrequency)
                .providerFrequency(providerFrequency)
                .uniqueCurrencies(new ArrayList<>(currencies))
                .generatedAt(Instant.now())
                .build();

        log.debug("Calculator summary generated: {} conversions, {} favorites",
                summary.getTotalConversions(), summary.getFavoriteCount());
        return summary;
    }

    public Map<String, Long> getPairFrequency() {
        return historyRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        h -> h.getFromCurrency() + "/" + h.getToCurrency(),
                        Collectors.counting()));
    }

    public Map<String, Long> getProviderFrequency() {
        return historyRepository.findAll().stream()
                .filter(h -> h.getProvider() != null)
                .collect(Collectors.groupingBy(
                        CalculatorHistory::getProvider,
                        Collectors.counting()));
    }
}
