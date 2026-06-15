package com.example.exchangerate.health;

import com.example.exchangerate.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditHealthIndicator implements HealthIndicator {

    private final AuditService auditService;

    @Override
    public String componentName() {
        return "audit";
    }

    @Override
    public ComponentHealth checkHealth() {
        long totalConversions = auditService.getTotalConversions();
        long successCount = auditService.getSuccessCount();
        long failureCount = auditService.getFailureCount();

        HealthStatus status = HealthStatus.UP;

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(status)
                .details(Map.of(
                        "totalConversions", totalConversions,
                        "successCount", successCount,
                        "failureCount", failureCount
                ))
                .timestamp(Instant.now())
                .build();
    }
}
