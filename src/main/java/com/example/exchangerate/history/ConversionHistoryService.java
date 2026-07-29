package com.example.exchangerate.history;

import com.example.exchangerate.config.ConversionHistoryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionHistoryService {

    private final ConversionHistoryRepository historyRepository;
    private final ConversionHistoryConfig config;

    public ConversionHistoryEntry recordConversion(ConversionHistoryEntry entry) {
        ConversionHistoryEntry saved = historyRepository.save(entry);
        log.info("Conversion recorded: id={} {}->{} amount={} rate={}",
                saved.getId(), saved.getFromCurrency(), saved.getToCurrency(),
                saved.getAmount(), saved.getRate());
        return saved;
    }

    public Optional<ConversionHistoryEntry> getEntry(String id) {
        return historyRepository.findById(id);
    }

    public List<ConversionHistoryEntry> getAllEntries() {
        return historyRepository.findAll();
    }

    public List<ConversionHistoryEntry> getEntries(int page, int size) {
        int maxSize = config.getMaxPageSize();
        if (size > maxSize) {
            size = maxSize;
        }
        return historyRepository.findAll(page, size);
    }

    public List<ConversionHistoryEntry> getEntriesByCurrencyPair(String from, String to) {
        return historyRepository.findByCurrencyPair(from, to);
    }

    public List<ConversionHistoryEntry> getEntriesByStatus(String status) {
        return historyRepository.findByStatus(status);
    }

    public long getTotalCount() {
        return historyRepository.count();
    }

    public long getCountByCurrencyPair(String from, String to) {
        return historyRepository.countByCurrencyPair(from, to);
    }

    public void deleteEntry(String id) {
        historyRepository.deleteById(id);
        log.info("History entry deleted: id={}", id);
    }

    public void clearHistory() {
        historyRepository.clearAll();
        log.info("History cleared");
    }

    public Map<String, Object> getStatistics() {
        List<ConversionHistoryEntry> allEntries = historyRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalConversions", allEntries.size());

        if (allEntries.isEmpty()) {
            stats.put("averageRate", BigDecimal.ZERO);
            stats.put("totalVolume", BigDecimal.ZERO);
            stats.put("successRate", BigDecimal.ZERO);
            return stats;
        }

        BigDecimal totalRate = allEntries.stream()
                .map(ConversionHistoryEntry::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("averageRate", totalRate.divide(BigDecimal.valueOf(allEntries.size()), 6, RoundingMode.HALF_UP));

        BigDecimal totalVolume = allEntries.stream()
                .map(ConversionHistoryEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalVolume", totalVolume);

        long successCount = allEntries.stream()
                .filter(e -> "SUCCESS".equals(e.getStatus()))
                .count();
        stats.put("successRate", BigDecimal.valueOf(successCount)
                .divide(BigDecimal.valueOf(allEntries.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));

        Map<String, Long> conversionsByPair = new HashMap<>();
        allEntries.forEach(e -> {
            String pair = e.getFromCurrency() + "/" + e.getToCurrency();
            conversionsByPair.merge(pair, 1L, Long::sum);
        });
        stats.put("conversionsByPair", conversionsByPair);

        return stats;
    }

    public Map<String, Object> getRecentActivity(int hours) {
        Instant cutoff = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<ConversionHistoryEntry> recent = historyRepository.findAll().stream()
                .filter(e -> e.getTimestamp().isAfter(cutoff))
                .toList();

        Map<String, Object> activity = new HashMap<>();
        activity.put("periodHours", hours);
        activity.put("totalConversions", recent.size());

        long successCount = recent.stream()
                .filter(e -> "SUCCESS".equals(e.getStatus()))
                .count();
        activity.put("successCount", successCount);
        activity.put("failureCount", recent.size() - successCount);

        if (!recent.isEmpty()) {
            BigDecimal totalVolume = recent.stream()
                    .map(ConversionHistoryEntry::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            activity.put("totalVolume", totalVolume);
        } else {
            activity.put("totalVolume", BigDecimal.ZERO);
        }

        return activity;
    }
}
