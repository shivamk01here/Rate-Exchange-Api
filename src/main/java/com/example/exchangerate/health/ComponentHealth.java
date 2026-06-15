package com.example.exchangerate.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ComponentHealth {
    private String componentName;
    private HealthStatus status;
    private Map<String, Object> details;
    private Instant timestamp;
}
