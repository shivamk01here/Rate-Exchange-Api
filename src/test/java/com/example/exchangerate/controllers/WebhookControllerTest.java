package com.example.exchangerate.controllers;

import com.example.exchangerate.webhook.Webhook;
import com.example.exchangerate.webhook.WebhookMetricsCollector;
import com.example.exchangerate.webhook.WebhookRepository;
import com.example.exchangerate.webhook.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhookControllerTest {

    private WebhookController controller;

    @BeforeEach
    void setUp() {
        WebhookRepository repository = new WebhookRepository();
        WebhookMetricsCollector metricsCollector = new WebhookMetricsCollector();
        WebhookService service = new WebhookService(repository, metricsCollector);
        controller = new WebhookController(service, metricsCollector);
    }

    @Test
    void createWebhook_returnsCreatedWebhook() {
        Webhook webhook = Webhook.builder()
                .url("https://example.com/webhook")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true)
                .build();

        Webhook result = controller.createWebhook(webhook);

        assertNotNull(result.getId());
        assertEquals("https://example.com/webhook", result.getUrl());
    }

    @Test
    void createWebhook_throwsWhenUrlMissing() {
        Webhook webhook = Webhook.builder()
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createWebhook(webhook));
    }

    @Test
    void createWebhook_throwsWhenEventsMissing() {
        Webhook webhook = Webhook.builder()
                .url("https://example.com/webhook")
                .enabled(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createWebhook(webhook));
    }

    @Test
    void getAllWebhooks_returnsAllWebhooks() {
        controller.createWebhook(Webhook.builder()
                .url("https://example.com/hook1")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());
        controller.createWebhook(Webhook.builder()
                .url("https://example.com/hook2")
                .events(List.of(Webhook.WebhookEvent.RATE_BELOW_THRESHOLD))
                .enabled(false).build());

        List<Webhook> all = controller.getAllWebhooks();

        assertEquals(2, all.size());
    }

    @Test
    void getWebhook_returnsWebhookById() {
        Webhook created = controller.createWebhook(Webhook.builder()
                .url("https://example.com/hook")
                .events(List.of(Webhook.WebhookEvent.RATE_ABOVE_THRESHOLD))
                .enabled(true).build());

        Webhook result = controller.getWebhook(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getWebhook_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getWebhook("bad-id"));
    }

    @Test
    void deleteWebhook_returnsSuccess() {
        Webhook created = controller.createWebhook(Webhook.builder()
                .url("https://example.com/hook")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());

        Map<String, String> result = controller.deleteWebhook(created.getId());

        assertEquals("deleted", result.get("status"));
        assertThrows(ResponseStatusException.class, () -> controller.getWebhook(created.getId()));
    }

    @Test
    void deleteWebhook_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteWebhook("bad-id"));
    }

    @Test
    void toggleWebhook_changesEnabledState() {
        Webhook created = controller.createWebhook(Webhook.builder()
                .url("https://example.com/hook")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(false).build());

        Webhook toggled = controller.toggleWebhook(created.getId(), Map.of("enabled", true));

        assertTrue(toggled.isEnabled());
    }

    @Test
    void getWebhookCount_returnsCount() {
        controller.createWebhook(Webhook.builder()
                .url("https://example.com/hook1")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());

        Map<String, Object> result = controller.getWebhookCount();

        assertEquals(1L, result.get("count"));
    }

    @Test
    void getWebhookStats_returnsMetrics() {
        Map<String, Object> stats = controller.getWebhookStats();

        assertNotNull(stats);
        assertTrue(stats.containsKey("creates"));
        assertTrue(stats.containsKey("deliveries"));
    }
}
