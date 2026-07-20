package com.example.exchangerate.webhook;

import com.example.exchangerate.alert.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDeliveryService {

    private final WebhookRepository webhookRepository;
    private final WebClient.Builder webClientBuilder;
    private final WebhookMetricsCollector metricsCollector;
    private final WebhookDeliveryLogRepository deliveryLogRepository;

    public void deliverAlertTriggered(Alert alert, BigDecimal currentRate) {
        List<Webhook> webhooks = webhookRepository.findEnabledByEvent(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED);
        webhooks.addAll(webhookRepository.findEnabledByEvent(Webhook.WebhookEvent.RATE_ABOVE_THRESHOLD));
        webhooks.addAll(webhookRepository.findEnabledByEvent(Webhook.WebhookEvent.RATE_BELOW_THRESHOLD));

        Map<String, Object> payload = Map.of(
                "event", "RATE_ALERT_TRIGGERED",
                "alertId", alert.getId(),
                "fromCurrency", alert.getFromCurrency(),
                "toCurrency", alert.getToCurrency(),
                "condition", alert.getCondition().name(),
                "threshold", alert.getThreshold(),
                "currentRate", currentRate,
                "timestamp", Instant.now().toString()
        );

        for (Webhook webhook : webhooks) {
            if (matchesEvent(webhook, alert.getCondition())) {
                metricsCollector.recordDelivery();
                deliverAsync(webhook, payload);
            }
        }
    }

    private boolean matchesEvent(Webhook webhook, Alert.AlertCondition condition) {
        if (webhook.getEvents().contains(Webhook.WebhookEvent.RATE_ALERT_TRIGGERED)) {
            return true;
        }
        if (condition == Alert.AlertCondition.RATE_ABOVE
                && webhook.getEvents().contains(Webhook.WebhookEvent.RATE_ABOVE_THRESHOLD)) {
            return true;
        }
        if (condition == Alert.AlertCondition.RATE_BELOW
                && webhook.getEvents().contains(Webhook.WebhookEvent.RATE_BELOW_THRESHOLD)) {
            return true;
        }
        return false;
    }

    private void deliverAsync(Webhook webhook, Map<String, Object> payload) {
        CompletableFuture.runAsync(() -> {
            try {
                WebClient client = webClientBuilder.baseUrl(webhook.getUrl()).build();

                WebClient.RequestHeadersSpec<?> request = client.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload);

                if (webhook.getSecret() != null && !webhook.getSecret().isBlank()) {
                    request.header("X-Webhook-Secret", webhook.getSecret());
                }

                request.retrieve()
                        .toBodilessEntity()
                        .doOnSuccess(response -> {
                            log.info("Webhook delivered: id={} url={} status={}",
                                    webhook.getId(), webhook.getUrl(), response.getStatusCode());
                            metricsCollector.recordDeliverySuccess();
                            webhookRepository.updateLastTriggered(webhook.getId(), Instant.now());
                            deliveryLogRepository.save(WebhookDeliveryLog.builder()
                                    .webhookId(webhook.getId())
                                    .webhookUrl(webhook.getUrl())
                                    .event("RATE_ALERT_TRIGGERED")
                                    .statusCode(response.getStatusCodeValue())
                                    .success(true)
                                    .deliveredAt(Instant.now())
                                    .build());
                        })
                        .doOnError(error -> {
                            log.warn("Webhook delivery failed: id={} url={} error={}",
                                    webhook.getId(), webhook.getUrl(), error.getMessage());
                            metricsCollector.recordDeliveryFailure();
                            webhookRepository.incrementFailureCount(webhook.getId());
                            deliveryLogRepository.save(WebhookDeliveryLog.builder()
                                    .webhookId(webhook.getId())
                                    .webhookUrl(webhook.getUrl())
                                    .event("RATE_ALERT_TRIGGERED")
                                    .statusCode(0)
                                    .success(false)
                                    .errorMessage(error.getMessage())
                                    .deliveredAt(Instant.now())
                                    .build());
                        })
                        .subscribe();
            } catch (Exception e) {
                log.error("Webhook delivery error: id={} url={}", webhook.getId(), webhook.getUrl(), e);
                metricsCollector.recordDeliveryFailure();
                webhookRepository.incrementFailureCount(webhook.getId());
                deliveryLogRepository.save(WebhookDeliveryLog.builder()
                        .webhookId(webhook.getId())
                        .webhookUrl(webhook.getUrl())
                        .event("RATE_ALERT_TRIGGERED")
                        .statusCode(0)
                        .success(false)
                        .errorMessage(e.getMessage())
                        .deliveredAt(Instant.now())
                        .build());
            }
        });
    }
}
