package com.example.exchangerate.alerthistory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AlertHistoryRepository {

    private final ConcurrentHashMap<String, AlertHistoryEntry> entries = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<AlertHistoryEntry> entryList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public AlertHistoryEntry save(AlertHistoryEntry entry) {
        String id = entry.getId() != null ? entry.getId() : String.valueOf(idCounter.incrementAndGet());
        AlertHistoryEntry stored = AlertHistoryEntry.builder()
                .id(id)
                .alertId(entry.getAlertId())
                .fromCurrency(entry.getFromCurrency())
                .toCurrency(entry.getToCurrency())
                .condition(entry.getCondition())
                .threshold(entry.getThreshold())
                .triggeredRate(entry.getTriggeredRate())
                .email(entry.getEmail())
                .phone(entry.getPhone())
                .emailSent(entry.isEmailSent())
                .whatsappSent(entry.isWhatsappSent())
                .webhookSent(entry.isWebhookSent())
                .triggeredAt(entry.getTriggeredAt() != null ? entry.getTriggeredAt() : java.time.Instant.now())
                .build();

        if (entries.putIfAbsent(id, stored) == null) {
            entryList.add(stored);
        } else {
            entries.put(id, stored);
            for (int i = 0; i < entryList.size(); i++) {
                if (id.equals(entryList.get(i).getId())) {
                    entryList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("AlertHistory saved: id={} alertId={} {}->{} rate={}",
                id, stored.getAlertId(), stored.getFromCurrency(),
                stored.getToCurrency(), stored.getTriggeredRate());
        return stored;
    }

    public Optional<AlertHistoryEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public List<AlertHistoryEntry> findAll() {
        return new ArrayList<>(entryList);
    }

    public List<AlertHistoryEntry> findAll(int page, int size) {
        List<AlertHistoryEntry> sorted = entryList.stream()
                .sorted(Comparator.comparing(AlertHistoryEntry::getTriggeredAt).reversed())
                .collect(Collectors.toList());
        int start = page * size;
        if (start >= sorted.size()) {
            return new ArrayList<>();
        }
        int end = Math.min(start + size, sorted.size());
        return new ArrayList<>(sorted.subList(start, end));
    }

    public List<AlertHistoryEntry> findByAlertId(String alertId) {
        return entryList.stream()
                .filter(e -> alertId.equals(e.getAlertId()))
                .sorted(Comparator.comparing(AlertHistoryEntry::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }

    public List<AlertHistoryEntry> findByCurrencyPair(String from, String to) {
        return entryList.stream()
                .filter(e -> from.equalsIgnoreCase(e.getFromCurrency())
                        && to.equalsIgnoreCase(e.getToCurrency()))
                .sorted(Comparator.comparing(AlertHistoryEntry::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }

    public List<AlertHistoryEntry> findByTriggeredAtAfter(java.time.Instant cutoff) {
        return entryList.stream()
                .filter(e -> e.getTriggeredAt().isAfter(cutoff))
                .sorted(Comparator.comparing(AlertHistoryEntry::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }

    public long countByTriggeredAtAfter(java.time.Instant cutoff) {
        return entryList.stream()
                .filter(e -> e.getTriggeredAt().isAfter(cutoff))
                .count();
    }

    public long count() {
        return entries.size();
    }

    public long countByAlertId(String alertId) {
        return entryList.stream()
                .filter(e -> alertId.equals(e.getAlertId()))
                .count();
    }

    public Map<String, Long> countTriggersByAlertId() {
        return entryList.stream()
                .collect(Collectors.groupingBy(AlertHistoryEntry::getAlertId, Collectors.counting()));
    }

    public void deleteById(String id) {
        AlertHistoryEntry removed = entries.remove(id);
        if (removed != null) {
            entryList.remove(removed);
            log.info("AlertHistory deleted: id={}", id);
        }
    }

    public void clearAll() {
        entries.clear();
        entryList.clear();
        log.info("AlertHistory cleared");
    }
}
