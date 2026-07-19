package com.example.exchangerate.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebhookServiceTest {

    private WebhookService webhookService;
    private WebhookRepository webhookRepository;

    @BeforeEach
    void setUp() {
        webhookRepository = new WebhookRepository();
        WebhookMetricsCollector metricsCollector = new WebhookMetricsCollector();
        webhookService = new WebhookService(webhookRepository, metricsCollector);
    }

    @Test
    void createWebhook_returnsSavedWebhookWithId() {
        Webhook webhook = Webhook.builder()
                .url("https://example.com/webhook")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true)
                .build();

        Webhook saved = webhookService.createWebhook(webhook);

        assertNotNull(saved.getId());
        assertEquals("https://example.com/webhook", saved.getUrl());
        assertTrue(saved.isEnabled());
    }

    @Test
    void getWebhook_returnsWebhookWhenExists() {
        Webhook webhook = Webhook.builder()
                .url("https://example.com/hook")
                .events(List.of(Webhook.WebhookEvent.RATE_ABOVE_THRESHOLD))
                .enabled(true)
                .build();
        Webhook saved = webhookService.createWebhook(webhook);

        Webhook found = webhookService.getWebhook(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getWebhook_returnsEmptyWhenNotFound() {
        assertTrue(webhookService.getWebhook("nonexistent").isEmpty());
    }

    @Test
    void getAllWebhooks_returnsAllCreatedWebhooks() {
        webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook1")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());
        webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook2")
                .events(List.of(Webhook.WebhookEvent.RATE_BELOW_THRESHOLD))
                .enabled(false).build());

        List<Webhook> all = webhookService.getAllWebhooks();

        assertEquals(2, all.size());
    }

    @Test
    void deleteWebhook_removesWebhook() {
        Webhook saved = webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());

        assertTrue(webhookService.deleteWebhook(saved.getId()));
        assertTrue(webhookService.getWebhook(saved.getId()).isEmpty());
    }

    @Test
    void deleteWebhook_returnsFalseForNonexistent() {
        assertFalse(webhookService.deleteWebhook("nonexistent"));
    }

    @Test
    void toggleWebhook_enablesAndDisables() {
        Webhook saved = webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(false).build());

        Webhook toggledOn = webhookService.toggleWebhook(saved.getId(), true);
        assertTrue(toggledOn.isEnabled());

        Webhook toggledOff = webhookService.toggleWebhook(saved.getId(), false);
        assertFalse(toggledOff.isEnabled());
    }

    @Test
    void toggleWebhook_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> webhookService.toggleWebhook("nonexistent", true));
    }

    @Test
    void getEnabledByEvent_returnsMatchingWebhooks() {
        webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook1")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());
        webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook2")
                .events(List.of(Webhook.WebhookEvent.RATE_ABOVE_THRESHOLD))
                .enabled(true).build());
        webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook3")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(false).build());

        List<Webhook> triggered = webhookService.getEnabledByEvent(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED);

        assertEquals(1, triggered.size());
        assertEquals("https://example.com/hook1", triggered.get(0).getUrl());
    }

    @Test
    void getWebhookCount_returnsCorrectCount() {
        assertEquals(0, webhookService.getWebhookCount());

        webhookService.createWebhook(Webhook.builder()
                .url("https://example.com/hook1")
                .events(List.of(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED))
                .enabled(true).build());

        assertEquals(1, webhookService.getWebhookCount());
    }
}
