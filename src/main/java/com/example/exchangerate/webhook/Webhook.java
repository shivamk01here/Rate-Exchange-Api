package com.example.exchangerate.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

    private String id;

    @NotBlank(message = "URL is required")
    private String url;

    private String secret;

    @NotEmpty(message = "At least one event is required")
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
