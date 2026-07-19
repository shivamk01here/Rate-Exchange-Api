package com.example.exchangerate.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookRepository webhookRepository;

    public Webhook createWebhook(Webhook webhook) {
        Webhook saved = webhookRepository.save(webhook);
        log.info("Webhook created: id={} url={} events={}",
                saved.getId(), saved.getUrl(), saved.getEvents());
        return saved;
    }

    public Optional<Webhook> getWebhook(String id) {
        return webhookRepository.findById(id);
    }

    public List<Webhook> getAllWebhooks() {
        return webhookRepository.findAll();
    }

    public List<Webhook> getEnabledByEvent(Webhook.WebhookEvent event) {
        return webhookRepository.findEnabledByEvent(event);
    }

    public boolean deleteWebhook(String id) {
        boolean deleted = webhookRepository.deleteById(id);
        if (deleted) {
            log.info("Webhook deleted: id={}", id);
        }
        return deleted;
    }

    public Webhook toggleWebhook(String id, boolean enabled) {
        return webhookRepository.findById(id)
                .map(existing -> {
                    Webhook updated = Webhook.builder()
                            .id(existing.getId())
                            .url(existing.getUrl())
                            .secret(existing.getSecret())
                            .events(existing.getEvents())
                            .enabled(enabled)
                            .createdAt(existing.getCreatedAt())
                            .lastTriggeredAt(existing.getLastTriggeredAt())
                            .failureCount(existing.getFailureCount())
                            .build();
                    Webhook saved = webhookRepository.save(updated);
                    log.info("Webhook {} toggled to enabled={}", id, enabled);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + id));
    }

    public long getWebhookCount() {
        return webhookRepository.count();
    }
}
