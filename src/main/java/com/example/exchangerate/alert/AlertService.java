package com.example.exchangerate.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    public Alert createAlert(Alert alert) {
        Alert saved = alertRepository.save(alert);
        log.info("Alert created: id={} {}->{} {} {} email={}",
                saved.getId(), saved.getFromCurrency(), saved.getToCurrency(),
                saved.getCondition(), saved.getThreshold(), saved.getEmail());
        return saved;
    }

    public Optional<Alert> getAlert(String id) {
        return alertRepository.findById(id);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public List<Alert> getAlertsByCurrencyPair(String from, String to) {
        return alertRepository.findByCurrencyPair(from, to);
    }

    public List<Alert> getAlertsByEmail(String email) {
        return alertRepository.findByEmail(email);
    }

    public boolean deleteAlert(String id) {
        boolean deleted = alertRepository.deleteById(id);
        if (deleted) {
            log.info("Alert deleted: id={}", id);
        }
        return deleted;
    }

    public Alert toggleAlert(String id, boolean enabled) {
        return alertRepository.findById(id)
                .map(existing -> {
                    Alert updated = Alert.builder()
                            .id(existing.getId())
                            .fromCurrency(existing.getFromCurrency())
                            .toCurrency(existing.getToCurrency())
                            .condition(existing.getCondition())
                            .threshold(existing.getThreshold())
                            .email(existing.getEmail())
                            .enabled(enabled)
                            .createdAt(existing.getCreatedAt())
                            .lastTriggeredAt(existing.getLastTriggeredAt())
                            .build();
                    Alert saved = alertRepository.save(updated);
                    log.info("Alert {} toggled to enabled={}", id, enabled);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
    }

    public long getAlertCount() {
        return alertRepository.count();
    }
}
