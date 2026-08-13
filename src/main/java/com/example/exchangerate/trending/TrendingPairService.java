package com.example.exchangerate.trending;

import com.example.exchangerate.history.ConversionHistoryEntry;
import com.example.exchangerate.history.ConversionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendingPairService {

    private final ConversionHistoryRepository historyRepository;

    public List<TrendingPair> getTrendingPairs(int limit) {
        List<TrendingPair> pairs = aggregate(historyRepository.findAll());
        return topByVolume(pairs, limit);
    }

    public List<TrendingPair> getTrendingPairsByCount(int limit) {
        List<TrendingPair> pairs = aggregate(historyRepository.findAll());
        return topByCount(pairs, limit);
    }

    public List<TrendingPair> getTrendingPairsSince(int hours, int limit) {
        Instant cutoff = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<ConversionHistoryEntry> recent = historyRepository.findAll().stream()
                .filter(e -> e.getTimestamp() != null && e.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
        return topByVolume(aggregate(recent), limit);
    }

    public long getDistinctPairCount() {
        return aggregate(historyRepository.findAll()).size();
    }

    private List<TrendingPair> aggregate(List<ConversionHistoryEntry> entries) {
        Map<String, TrendingPair> byKey = new HashMap<>();
        for (ConversionHistoryEntry entry : entries) {
            String key = entry.getFromCurrency() + "/" + entry.getToCurrency();
            TrendingPair existing = byKey.get(key);
            BigDecimal volume = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
            if (existing == null) {
                byKey.put(key, TrendingPair.builder()
                        .fromCurrency(entry.getFromCurrency())
                        .toCurrency(entry.getToCurrency())
                        .conversionCount(1)
                        .totalVolume(volume)
                        .latestTimestamp(entry.getTimestamp())
                        .build());
            } else {
                Instant latest = existing.getLatestTimestamp();
                if (entry.getTimestamp() != null
                        && (latest == null || entry.getTimestamp().isAfter(latest))) {
                    latest = entry.getTimestamp();
                }
                byKey.put(key, TrendingPair.builder()
                        .fromCurrency(existing.getFromCurrency())
                        .toCurrency(existing.getToCurrency())
                        .conversionCount(existing.getConversionCount() + 1)
                        .totalVolume(existing.getTotalVolume().add(volume))
                        .latestTimestamp(latest)
                        .build());
            }
        }
        log.debug("Aggregated {} history entries into {} pairs", entries.size(), byKey.size());
        return new ArrayList<>(byKey.values());
    }

    private List<TrendingPair> topByVolume(List<TrendingPair> pairs, int limit) {
        return pairs.stream()
                .sorted(Comparator.comparing(TrendingPair::getTotalVolume).reversed())
                .limit(Math.max(limit, 0))
                .collect(Collectors.toList());
    }

    private List<TrendingPair> topByCount(List<TrendingPair> pairs, int limit) {
        return pairs.stream()
                .sorted(Comparator.comparingLong(TrendingPair::getConversionCount).reversed())
                .limit(Math.max(limit, 0))
                .collect(Collectors.toList());
    }
}
