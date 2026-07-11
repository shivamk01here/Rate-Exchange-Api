package com.example.exchangerate.health;

import com.example.exchangerate.config.FavoritesConfig;
import com.example.exchangerate.favorites.FavoritePairRepository;
import com.example.exchangerate.favorites.FavoritesMetricsCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FavoritesHealthIndicator implements HealthIndicator {

    private final FavoritePairRepository favoritePairRepository;
    private final FavoritesMetricsCollector metrics;
    private final FavoritesConfig favoritesConfig;

    @Override
    public String componentName() {
        return "favorites";
    }

    @Override
    public ComponentHealth checkHealth() {
        long currentCount = favoritePairRepository.count();
        int maxFavorites = favoritesConfig.getMaxFavorites();
        double usageRatio = maxFavorites > 0 ? (double) currentCount / maxFavorites : 0;

        HealthStatus status;
        if (usageRatio >= 0.95) {
            status = HealthStatus.DEGRADED;
        } else if (!favoritesConfig.isEnabled()) {
            status = HealthStatus.DOWN;
        } else {
            status = HealthStatus.UP;
        }

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(status)
                .details(Map.of(
                        "enabled", favoritesConfig.isEnabled(),
                        "currentCount", currentCount,
                        "maxFavorites", maxFavorites,
                        "usageRatio", String.format("%.2f", usageRatio),
                        "metrics", metrics.getStats()
                ))
                .timestamp(Instant.now())
                .build();
    }
}
