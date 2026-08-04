package com.example.exchangerate.alerthistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistoryStats {

    private long totalTriggers;
    private long uniqueAlerts;
    private long uniqueCurrencyPairs;
    private List<Map.Entry<String, Long>> topPairs;
    private long triggersLast24h;
    private long triggersLast7d;
    private long emailSentCount;
    private long whatsappSentCount;
    private long webhookSentCount;
    private Instant generatedAt;
}
