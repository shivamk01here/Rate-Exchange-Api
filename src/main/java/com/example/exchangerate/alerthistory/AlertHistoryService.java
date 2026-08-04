package com.example.exchangerate.alerthistory;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.config.AlertHistoryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertHistoryService {

    private final AlertHistoryRepository alertHistoryRepository;
    private final AlertHistoryConfig config;

    public AlertHistoryEntry recordTrigger(Alert alert, BigDecimal triggeredRate) {
        AlertHistoryEntry entry = AlertHistoryEntry.builder()
                .alertId(alert.getId())
                .fromCurrency(alert.getFromCurrency())
                .toCurrency(alert.getToCurrency())
                .condition(alert.getCondition())
                .threshold(alert.getThreshold())
                .triggeredRate(triggeredRate)
                .email(alert.getEmail())
                .phone(alert.getPhone())
                .emailSent(true)
                .whatsappSent(alert.getPhone() != null)
                .webhookSent(true)
                .build();

        AlertHistoryEntry saved = alertHistoryRepository.save(entry);
        log.info("Alert trigger recorded: id={} alertId={} {}->{} rate={} threshold={}",
                saved.getId(), saved.getAlertId(), saved.getFromCurrency(),
                saved.getToCurrency(), saved.getTriggeredRate(), saved.getThreshold());

        trimToMaxEntries();
        return saved;
    }

    public Optional<AlertHistoryEntry> getEntry(String id) {
        return alertHistoryRepository.findById(id);
    }

    public List<AlertHistoryEntry> getAllEntries() {
        return alertHistoryRepository.findAll();
    }

    public List<AlertHistoryEntry> getEntries(int page, int size) {
        int maxSize = config.getMaxPageSize();
        if (size > maxSize) {
            size = maxSize;
        }
        return alertHistoryRepository.findAll(page, size);
    }

    public List<AlertHistoryEntry> getEntriesByAlertId(String alertId) {
        return alertHistoryRepository.findByAlertId(alertId);
    }

    public List<AlertHistoryEntry> getEntriesByCurrencyPair(String from, String to) {
        return alertHistoryRepository.findByCurrencyPair(from, to);
    }

    public long getTotalCount() {
        return alertHistoryRepository.count();
    }

    public long getCountByAlertId(String alertId) {
        return alertHistoryRepository.countByAlertId(alertId);
    }

    public AlertHistoryStats getStats() {
        List<AlertHistoryEntry> all = alertHistoryRepository.findAll();
        Instant now = Instant.now();

        Map<String, Long> pairCounts = all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getFromCurrency() + "/" + e.getToCurrency(),
                        Collectors.counting()));

        List<Map.Entry<String, Long>> topPairs = pairCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(config.getStatsTopPairsLimit())
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return AlertHistoryStats.builder()
                .totalTriggers(all.size())
                .uniqueAlerts(all.stream().map(AlertHistoryEntry::getAlertId).distinct().count())
                .uniqueCurrencyPairs(pairCounts.size())
                .topPairs(topPairs)
                .triggersLast24h(alertHistoryRepository.countByTriggeredAtAfter(now.minus(24, ChronoUnit.HOURS)))
                .triggersLast7d(alertHistoryRepository.countByTriggeredAtAfter(now.minus(7, ChronoUnit.DAYS)))
                .emailSentCount(all.stream().filter(AlertHistoryEntry::isEmailSent).count())
                .whatsappSentCount(all.stream().filter(AlertHistoryEntry::isWhatsappSent).count())
                .webhookSentCount(all.stream().filter(AlertHistoryEntry::isWebhookSent).count())
                .generatedAt(now)
                .build();
    }

    public List<AlertHistoryEntry> getRecentTriggers(int hours) {
        int window = hours > 0 ? hours : config.getRecentWindowHours();
        Instant cutoff = Instant.now().minus(window, ChronoUnit.HOURS);
        return alertHistoryRepository.findByTriggeredAtAfter(cutoff);
    }

    public void deleteEntry(String id) {
        alertHistoryRepository.deleteById(id);
        log.info("Alert history entry deleted: id={}", id);
    }

    public void clearHistory() {
        alertHistoryRepository.clearAll();
        log.info("Alert history cleared");
    }

    private void trimToMaxEntries() {
        int maxEntries = config.getMaxEntries();
        List<AlertHistoryEntry> all = alertHistoryRepository.findAll();
        if (all.size() > maxEntries) {
            int toRemove = all.size() - maxEntries;
            all.stream()
                    .sorted((a, b) -> a.getTriggeredAt().compareTo(b.getTriggeredAt()))
                    .limit(toRemove)
                    .forEach(e -> alertHistoryRepository.deleteById(e.getId()));
            log.debug("Trimmed {} oldest alert history entries", toRemove);
        }
    }
}
