package com.example.exchangerate.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final List<HealthIndicator> indicators;

    @Value("${spring.application.name:exchange-rate-service}")
    private String appName;

    public Map<String, Object> getHealth() {
        Map<String, Object> components = new HashMap<>();
        HealthStatus overall = HealthStatus.UP;

        for (HealthIndicator indicator : indicators) {
            try {
                ComponentHealth health = indicator.checkHealth();
                components.put(indicator.componentName(), health);
                if (health.getStatus() == HealthStatus.DOWN) {
                    overall = HealthStatus.DOWN;
                } else if (health.getStatus() == HealthStatus.DEGRADED && overall != HealthStatus.DOWN) {
                    overall = HealthStatus.DEGRADED;
                }
            } catch (Exception e) {
                log.error("Health check failed for component: {}", indicator.componentName(), e);
                components.put(indicator.componentName(), ComponentHealth.builder()
                        .componentName(indicator.componentName())
                        .status(HealthStatus.DOWN)
                        .details(Map.of("error", e.getMessage()))
                        .timestamp(Instant.now())
                        .build());
                overall = HealthStatus.DOWN;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("service", appName);
        result.put("status", overall);
        result.put("timestamp", Instant.now().toString());
        result.put("components", components);
        return result;
    }
}
