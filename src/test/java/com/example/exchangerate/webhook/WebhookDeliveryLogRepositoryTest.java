package com.example.exchangerate.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebhookDeliveryLogRepositoryTest {

    private WebhookDeliveryLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new WebhookDeliveryLogRepository();
    }

    @Test
    void save_storesDeliveryLog() {
        WebhookDeliveryLog log = WebhookDeliveryLog.builder()
                .webhookId("wh-1")
                .webhookUrl("https://example.com/hook")
                .event("RATE_ALERT_TRIGGERED")
                .statusCode(200)
                .success(true)
                .build();

        WebhookDeliveryLog saved = repository.save(log);

        assertNotNull(saved.getId());
        assertTrue(saved.isSuccess());
    }

    @Test
    void findAll_returnsAllLogs() {
        repository.save(WebhookDeliveryLog.builder()
                .webhookId("wh-1").success(true).build());
        repository.save(WebhookDeliveryLog.builder()
                .webhookId("wh-2").success(false).build());

        List<WebhookDeliveryLog> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findByWebhookId_returnsMatchingLogs() {
        repository.save(WebhookDeliveryLog.builder()
                .webhookId("wh-1").success(true).build());
        repository.save(WebhookDeliveryLog.builder()
                .webhookId("wh-1").success(false).build());
        repository.save(WebhookDeliveryLog.builder()
                .webhookId("wh-2").success(true).build());

        List<WebhookDeliveryLog> wh1Logs = repository.findByWebhookId("wh-1");

        assertEquals(2, wh1Logs.size());
    }

    @Test
    void findRecent_returnsLimitedLogs() {
        for (int i = 0; i < 10; i++) {
            repository.save(WebhookDeliveryLog.builder()
                    .webhookId("wh-" + i).success(true).build());
        }

        List<WebhookDeliveryLog> recent = repository.findRecent(3);

        assertEquals(3, recent.size());
    }

    @Test
    void countSuccess_returnsSuccessCount() {
        repository.save(WebhookDeliveryLog.builder().webhookId("w1").success(true).build());
        repository.save(WebhookDeliveryLog.builder().webhookId("w2").success(false).build());
        repository.save(WebhookDeliveryLog.builder().webhookId("w3").success(true).build());

        assertEquals(2, repository.countSuccess());
        assertEquals(1, repository.countFailures());
    }

    @Test
    void clear_removesAllLogs() {
        repository.save(WebhookDeliveryLog.builder().webhookId("w1").success(true).build());
        repository.save(WebhookDeliveryLog.builder().webhookId("w2").success(true).build());

        repository.clear();

        assertEquals(0, repository.count());
    }
}
