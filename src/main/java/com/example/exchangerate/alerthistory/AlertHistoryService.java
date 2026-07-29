package com.example.exchangerate.alerthistory;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.config.AlertHistoryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
