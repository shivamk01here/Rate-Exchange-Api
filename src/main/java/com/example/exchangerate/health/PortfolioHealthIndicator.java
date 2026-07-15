package com.example.exchangerate.health;

import com.example.exchangerate.config.PortfolioConfig;
import com.example.exchangerate.portfolio.PortfolioMetricsCollector;
import com.example.exchangerate.portfolio.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortfolioHealthIndicator implements HealthIndicator {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMetricsCollector metrics;
    private final PortfolioConfig portfolioConfig;

    @Override
    public String componentName() {
        return "portfolio";
    }

    @Override
    public ComponentHealth checkHealth() {
        long currentCount = portfolioRepository.count();
        int maxPortfolios = portfolioConfig.getMaxPortfolios();
        double usageRatio = maxPortfolios > 0 ? (double) currentCount / maxPortfolios : 0;

        HealthStatus status;
        if (usageRatio >= 0.95) {
            status = HealthStatus.DEGRADED;
        } else if (!portfolioConfig.isEnabled()) {
            status = HealthStatus.DOWN;
        } else {
            status = HealthStatus.UP;
        }

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(status)
                .details(Map.of(
                        "enabled", portfolioConfig.isEnabled(),
                        "currentCount", currentCount,
                        "maxPortfolios", maxPortfolios,
                        "usageRatio", String.format("%.2f", usageRatio),
                        "metrics", metrics.getStats()
                ))
                .timestamp(Instant.now())
                .build();
    }
}
