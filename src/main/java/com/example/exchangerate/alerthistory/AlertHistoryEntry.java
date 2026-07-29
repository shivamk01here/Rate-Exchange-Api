package com.example.exchangerate.alerthistory;

import com.example.exchangerate.alert.Alert.AlertCondition;
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
public class AlertHistoryEntry {

    private String id;
    private String alertId;
    private String fromCurrency;
    private String toCurrency;
    private AlertCondition condition;
    private BigDecimal threshold;
    private BigDecimal triggeredRate;
    private String email;
    private String phone;
    @Builder.Default private boolean emailSent = true;
    @Builder.Default private boolean whatsappSent = false;
    @Builder.Default private boolean webhookSent = false;
    @Builder.Default private Instant triggeredAt = Instant.now();
}
