package com.example.exchangerate.webhook;

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
public class WebhookRepository {

    private final ConcurrentHashMap<String, Webhook> webhooks = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Webhook> webhookList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public Webhook save(Webhook webhook) {
        String id = webhook.getId() != null ? webhook.getId() : String.valueOf(idCounter.incrementAndGet());
        Webhook stored = Webhook.builder()
                .id(id)
                .url(webhook.getUrl())
                .secret(webhook.getSecret())
                .events(webhook.getEvents())
                .enabled(webhook.isEnabled())
                .createdAt(webhook.getCreatedAt() != null ? webhook.getCreatedAt() : java.time.Instant.now())
                .lastTriggeredAt(webhook.getLastTriggeredAt())
                .failureCount(webhook.getFailureCount())
                .build();

        if (webhooks.putIfAbsent(id, stored) == null) {
            webhookList.add(stored);
        } else {
            webhooks.put(id, stored);
            for (int i = 0; i < webhookList.size(); i++) {
                if (id.equals(webhookList.get(i).getId())) {
                    webhookList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("Webhook saved: id={} url={}", id, stored.getUrl());
        return stored;
    }

    public Optional<Webhook> findById(String id) {
        return Optional.ofNullable(webhooks.get(id));
    }

    public List<Webhook> findAll() {
        return new ArrayList<>(webhookList);
    }

    public List<Webhook> findEnabledByEvent(Webhook.WebhookEvent event) {
        return webhookList.stream()
                .filter(Webhook::isEnabled)
                .filter(w -> w.getEvents() != null && w.getEvents().contains(event))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        Webhook removed = webhooks.remove(id);
        if (removed != null) {
            webhookList.remove(removed);
            log.info("Webhook deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return webhooks.size();
    }

    public void updateLastTriggered(String id, java.time.Instant timestamp) {
        Webhook existing = webhooks.get(id);
        if (existing != null) {
            Webhook updated = Webhook.builder()
                    .id(existing.getId())
                    .url(existing.getUrl())
                    .secret(existing.getSecret())
                    .events(existing.getEvents())
                    .enabled(existing.isEnabled())
                    .createdAt(existing.getCreatedAt())
                    .lastTriggeredAt(timestamp)
                    .failureCount(existing.getFailureCount())
                    .build();
            webhooks.put(id, updated);
            for (int i = 0; i < webhookList.size(); i++) {
                if (id.equals(webhookList.get(i).getId())) {
                    webhookList.set(i, updated);
                    break;
                }
            }
        }
    }

    public void incrementFailureCount(String id) {
        Webhook existing = webhooks.get(id);
        if (existing != null) {
            Webhook updated = Webhook.builder()
                    .id(existing.getId())
                    .url(existing.getUrl())
                    .secret(existing.getSecret())
                    .events(existing.getEvents())
                    .enabled(existing.isEnabled())
                    .createdAt(existing.getCreatedAt())
                    .lastTriggeredAt(existing.getLastTriggeredAt())
                    .failureCount(existing.getFailureCount() + 1)
                    .build();
            webhooks.put(id, updated);
            for (int i = 0; i < webhookList.size(); i++) {
                if (id.equals(webhookList.get(i).getId())) {
                    webhookList.set(i, updated);
                    break;
                }
            }
        }
    }
}
