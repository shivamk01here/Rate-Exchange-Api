package com.example.exchangerate.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class WebhookMetricsCollector {

    private final AtomicLong createCount = new AtomicLong(0);
    private final AtomicLong deleteCount = new AtomicLong(0);
    private final AtomicLong deliveryCount = new AtomicLong(0);
    private final AtomicLong deliverySuccessCount = new AtomicLong(0);
    private final AtomicLong deliveryFailureCount = new AtomicLong(0);

    public void recordCreate() {
        createCount.incrementAndGet();
    }

    public void recordDelete() {
        deleteCount.incrementAndGet();
    }

    public void recordDelivery() {
        deliveryCount.incrementAndGet();
    }

    public void recordDeliverySuccess() {
        deliverySuccessCount.incrementAndGet();
    }

    public void recordDeliveryFailure() {
        deliveryFailureCount.incrementAndGet();
    }

    public Map<String, Object> getStats() {
        long deliveries = deliveryCount.get();
        long successes = deliverySuccessCount.get();
        return Map.of(
                "creates", createCount.get(),
                "deletes", deleteCount.get(),
                "deliveries", deliveries,
                "successes", successes,
                "failures", deliveryFailureCount.get(),
                "successRate", deliveries > 0 ? String.format("%.1f%%", (successes * 100.0 / deliveries)) : "0.0%"
        );
    }

    public void reset() {
        createCount.set(0);
        deleteCount.set(0);
        deliveryCount.set(0);
        deliverySuccessCount.set(0);
        deliveryFailureCount.set(0);
        log.info("Webhook metrics reset");
    }
}
