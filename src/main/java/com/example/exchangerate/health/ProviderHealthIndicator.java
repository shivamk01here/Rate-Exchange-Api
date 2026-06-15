package com.example.exchangerate.health;

import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.providers.ProviderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProviderHealthIndicator implements HealthIndicator {

    private final ProviderFactory providerFactory;

    @Override
    public String componentName() {
        return "providers";
    }

    @Override
    public ComponentHealth checkHealth() {
        Map<String, Object> providerDetails = new LinkedHashMap<>();
        boolean allUp = true;

        for (ProviderCodes code : ProviderCodes.values()) {
            try {
                providerFactory.getProvider(code);
                providerDetails.put(code.name(), "REGISTERED");
            } catch (IllegalArgumentException e) {
                providerDetails.put(code.name(), "UNREGISTERED");
                allUp = false;
            }
        }

        HealthStatus status = allUp ? HealthStatus.UP : HealthStatus.DEGRADED;

        return ComponentHealth.builder()
                .componentName(componentName())
                .status(status)
                .details(Map.of(
                        "providerCount", ProviderCodes.values().length,
                        "providers", providerDetails
                ))
                .timestamp(Instant.now())
                .build();
    }
}
