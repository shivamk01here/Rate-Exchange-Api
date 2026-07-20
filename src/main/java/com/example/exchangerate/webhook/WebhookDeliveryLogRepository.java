package com.example.exchangerate.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebhookDeliveryLogRepository {

    private final CopyOnWriteArrayList<WebhookDeliveryLog> logs = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public WebhookDeliveryLog save(WebhookDeliveryLog logEntry) {
        String id = logEntry.getId() != null ? logEntry.getId() : String.valueOf(idCounter.incrementAndGet());
        WebhookDeliveryLog stored = WebhookDeliveryLog.builder()
                .id(id)
                .webhookId(logEntry.getWebhookId())
                .webhookUrl(logEntry.getWebhookUrl())
                .event(logEntry.getEvent())
                .statusCode(logEntry.getStatusCode())
                .success(logEntry.isSuccess())
                .errorMessage(logEntry.getErrorMessage())
                .deliveredAt(logEntry.getDeliveredAt() != null ? logEntry.getDeliveredAt() : java.time.Instant.now())
                .build();
        logs.add(stored);
        log.debug("Delivery log saved: id={} webhookId={} success={}", id, stored.getWebhookId(), stored.isSuccess());
        return stored;
    }

    public List<WebhookDeliveryLog> findAll() {
        return new ArrayList<>(logs);
    }

    public List<WebhookDeliveryLog> findByWebhookId(String webhookId) {
        return logs.stream()
                .filter(l -> webhookId.equals(l.getWebhookId()))
                .collect(Collectors.toList());
    }

    public List<WebhookDeliveryLog> findRecent(int limit) {
        int size = logs.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(logs.subList(fromIndex, size));
    }

    public long count() {
        return logs.size();
    }

    public long countSuccess() {
        return logs.stream().filter(WebhookDeliveryLog::isSuccess).count();
    }

    public long countFailures() {
        return logs.stream().filter(l -> !l.isSuccess()).count();
    }

    public void clear() {
        logs.clear();
        log.info("Delivery logs cleared");
    }
}
