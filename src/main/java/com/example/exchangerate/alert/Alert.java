package com.example.exchangerate.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    private String id;
    private String fromCurrency;
    private String toCurrency;
    private AlertCondition condition;
    private BigDecimal threshold;
    private String email;
    private String phone;
    private boolean enabled;
    @Builder.Default private Instant createdAt = Instant.now();
    private Instant lastTriggeredAt;

    public enum AlertCondition {
        RATE_ABOVE,
        RATE_BELOW
    }

    public boolean shouldTrigger(BigDecimal currentRate) {
        if (!enabled || currentRate == null) {
            return false;
        }
        return switch (condition) {
            case RATE_ABOVE -> currentRate.compareTo(threshold) > 0;
            case RATE_BELOW -> currentRate.compareTo(threshold) < 0;
        };
    }
}
