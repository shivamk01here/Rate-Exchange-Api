package com.example.exchangerate.controllers;

import com.example.exchangerate.webhook.Webhook;
import com.example.exchangerate.webhook.WebhookDeliveryLog;
import com.example.exchangerate.webhook.WebhookDeliveryLogRepository;
import com.example.exchangerate.webhook.WebhookMetricsCollector;
import com.example.exchangerate.webhook.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final WebhookMetricsCollector metricsCollector;
    private final WebhookDeliveryLogRepository deliveryLogRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Webhook createWebhook(@Valid @RequestBody Webhook webhook) {
        if (webhook.getUrl() == null || webhook.getUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL is required");
        }
        if (webhook.getEvents() == null || webhook.getEvents().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one event is required");
        }
        log.info("Creating webhook: url={} events={}", webhook.getUrl(), webhook.getEvents());
        return webhookService.createWebhook(webhook);
    }

    @GetMapping
    public List<Webhook> getAllWebhooks() {
        return webhookService.getAllWebhooks();
    }

    @GetMapping("/{id}")
    public Webhook getWebhook(@PathVariable String id) {
        return webhookService.getWebhook(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Webhook not found: " + id));
    }

    @GetMapping("/by-event")
    public List<Webhook> getWebhooksByEvent(@RequestParam Webhook.WebhookEvent event) {
        return webhookService.getEnabledByEvent(event);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteWebhook(@PathVariable String id) {
        boolean deleted = webhookService.deleteWebhook(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Webhook not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PatchMapping("/{id}/toggle")
    public Webhook toggleWebhook(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", true);
        return webhookService.toggleWebhook(id, enabled);
    }

    @GetMapping("/count")
    public Map<String, Object> getWebhookCount() {
        return Map.of("count", webhookService.getWebhookCount());
    }

    @GetMapping("/stats")
    public Map<String, Object> getWebhookStats() {
        return metricsCollector.getStats();
    }

    @GetMapping("/delivery-logs")
    public List<WebhookDeliveryLog> getDeliveryLogs(@RequestParam(defaultValue = "100") int limit) {
        return deliveryLogRepository.findRecent(limit);
    }

    @GetMapping("/delivery-logs/{webhookId}")
    public List<WebhookDeliveryLog> getDeliveryLogsByWebhook(@PathVariable String webhookId) {
        return deliveryLogRepository.findByWebhookId(webhookId);
    }
}
