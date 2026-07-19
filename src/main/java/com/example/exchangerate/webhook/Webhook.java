package com.example.exchangerate.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    private String id;
    private String url;
    private String secret;
    private List<WebhookEvent> events;
    private boolean enabled;
    @Builder.Default private Instant createdAt = Instant.now();
    private Instant lastTriggeredAt;
    @Builder.Default private int failureCount = 0;

    public enum WebhookEvent {
        RATE_ALERT_TRIGGERED,
        RATE_ABOVE_THRESHOLD,
        RATE_BELOW_THRESHOLD
    }
}
