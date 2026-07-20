package com.example.exchangerate.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeliveryLog {

    private String id;
    private String webhookId;
    private String webhookUrl;
    private String event;
    private int statusCode;
    private boolean success;
    private String errorMessage;
    @Builder.Default private Instant deliveredAt = Instant.now();
}
