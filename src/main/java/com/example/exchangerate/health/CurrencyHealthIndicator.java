package com.example.exchangerate.health;

import com.example.exchangerate.services.CurrencyCacheService;
import com.example.exchangerate.services.CurrencyMetricsCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CurrencyHealthIndicator implements HealthIndicator {

    private final CurrencyCacheService currencyCacheService;
    private final CurrencyMetricsCollector metrics;

    @Override
    public String componentName() {
        return "currency";
    }

    @Override
    public ComponentHealth checkHealth() {
        int supportedCount = currencyCacheService.getSupportedCurrencies().size();

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(HealthStatus.UP)
                .details(Map.of(
                        "supportedCurrencies", supportedCount,
                        "totalLookups", metrics.getTotalLookups(),
                        "cacheHits", metrics.getCacheHits(),
                        "cacheMisses", metrics.getCacheMisses(),
                        "cacheHitRate", String.format("%.2f", metrics.getCacheHitRate())
                ))
                .timestamp(Instant.now())
                .build();
    }
}
