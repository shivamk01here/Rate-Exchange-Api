package com.example.exchangerate.health;

import com.example.exchangerate.config.CacheConfig;
import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.services.RateCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CacheHealthIndicator implements HealthIndicator {

    private final RateCacheService rateCacheService;
    private final CacheMetricsCollector metrics;
    private final CacheConfig cacheConfig;

    @Override
    public String componentName() {
        return "cache";
    }

    @Override
    public ComponentHealth checkHealth() {
        int currentSize = rateCacheService.size();
        int maxSize = cacheConfig.getMaxSize();
        double usageRatio = maxSize > 0 ? (double) currentSize / maxSize : 0;

        HealthStatus status;
        if (usageRatio >= 0.95) {
            status = HealthStatus.DEGRADED;
        } else {
            status = HealthStatus.UP;
        }

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(status)
                .details(Map.of(
                        "size", currentSize,
                        "maxSize", maxSize,
                        "usageRatio", String.format("%.2f", usageRatio),
                        "hits", metrics.getHits(),
                        "misses", metrics.getMisses(),
                        "evictions", metrics.getEvictions(),
                        "hitRate", String.format("%.2f", metrics.getHitRate())
                ))
                .timestamp(Instant.now())
                .build();
    }
}
