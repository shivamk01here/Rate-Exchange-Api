package com.example.exchangerate.health;

import com.example.exchangerate.trend.RateTrendConfig;
import com.example.exchangerate.trend.RateTrendMetricsCollector;
import com.example.exchangerate.trend.RateTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateTrendHealthIndicator implements HealthIndicator {

    private final RateTrendService rateTrendService;
    private final RateTrendMetricsCollector metricsCollector;
    private final RateTrendConfig trendConfig;

    @Override
    public String componentName() {
        return "rate-trend";
    }

    @Override
    public ComponentHealth checkHealth() {
        RateTrendService.MapStats stats = rateTrendService.getStats();
        long totalSnapshots = stats.totalSnapshots();
        int maxPerPair = trendConfig.getMaxSnapshotsPerPair();

        HealthStatus status;
        if (!trendConfig.isEnabled()) {
            status = HealthStatus.DOWN;
        } else if (totalSnapshots > maxPerPair * 50) {
            status = HealthStatus.DEGRADED;
        } else {
            status = HealthStatus.UP;
        }

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(status)
                .details(Map.of(
                        "enabled", trendConfig.isEnabled(),
                        "totalSnapshots", totalSnapshots,
                        "maxSnapshotsPerPair", maxPerPair,
                        "pairCounts", stats.pairCounts(),
                        "metrics", metricsCollector.getStats()
                ))
                .timestamp(Instant.now())
                .build();
    }
}
