package com.example.exchangerate.alert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AlertRepository {

    private final ConcurrentHashMap<String, Alert> alerts = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Alert> alertList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public Alert save(Alert alert) {
        String id = alert.getId() != null ? alert.getId() : String.valueOf(idCounter.incrementAndGet());
        Alert stored = Alert.builder()
                .id(id)
                .fromCurrency(alert.getFromCurrency())
                .toCurrency(alert.getToCurrency())
                .condition(alert.getCondition())
                .threshold(alert.getThreshold())
                .email(alert.getEmail())
                .phone(alert.getPhone())
                .enabled(alert.isEnabled())
                .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt() : java.time.Instant.now())
                .lastTriggeredAt(alert.getLastTriggeredAt())
                .build();

        if (alerts.putIfAbsent(id, stored) == null) {
            alertList.add(stored);
        } else {
            alerts.put(id, stored);
            int index = -1;
            for (int i = 0; i < alertList.size(); i++) {
                if (id.equals(alertList.get(i).getId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                alertList.set(index, stored);
            }
        }

        log.debug("Alert saved: id={} {}->{} {} {}", id,
                stored.getFromCurrency(), stored.getToCurrency(),
                stored.getCondition(), stored.getThreshold());
        return stored;
    }

    public Optional<Alert> findById(String id) {
        return Optional.ofNullable(alerts.get(id));
    }

    public List<Alert> findAll() {
        return new ArrayList<>(alertList);
    }

    public List<Alert> findByCurrencyPair(String from, String to) {
        return alertList.stream()
                .filter(a -> from.equalsIgnoreCase(a.getFromCurrency())
                        && to.equalsIgnoreCase(a.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<Alert> findByEmail(String email) {
        return alertList.stream()
                .filter(a -> email.equalsIgnoreCase(a.getEmail()))
                .collect(Collectors.toList());
    }

    public List<Alert> findEnabledAlerts() {
        return alertList.stream()
                .filter(Alert::isEnabled)
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        Alert removed = alerts.remove(id);
        if (removed != null) {
            alertList.remove(removed);
            log.info("Alert deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return alerts.size();
    }

    public void updateLastTriggered(String id, java.time.Instant timestamp) {
        Alert existing = alerts.get(id);
        if (existing != null) {
            Alert updated = Alert.builder()
                    .id(existing.getId())
                    .fromCurrency(existing.getFromCurrency())
                    .toCurrency(existing.getToCurrency())
                    .condition(existing.getCondition())
                    .threshold(existing.getThreshold())
                    .email(existing.getEmail())
                    .phone(existing.getPhone())
                    .enabled(existing.isEnabled())
                    .createdAt(existing.getCreatedAt())
                    .lastTriggeredAt(timestamp)
                    .build();
            alerts.put(id, updated);
            for (int i = 0; i < alertList.size(); i++) {
                if (id.equals(alertList.get(i).getId())) {
                    alertList.set(i, updated);
                    break;
                }
            }
        }
    }
}
