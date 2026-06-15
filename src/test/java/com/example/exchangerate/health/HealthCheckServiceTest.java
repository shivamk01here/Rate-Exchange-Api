package com.example.exchangerate.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckServiceTest {

    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckService(List.of(
                new HealthIndicator() {
                    @Override
                    public String componentName() { return "up-component"; }

                    @Override
                    public ComponentHealth checkHealth() {
                        return ComponentHealth.builder()
                                .componentName("up-component")
                                .status(HealthStatus.UP)
                                .details(Map.of("key", "value"))
                                .timestamp(Instant.now())
                                .build();
                    }
                },
                new HealthIndicator() {
                    @Override
                    public String componentName() { return "degraded-component"; }

                    @Override
                    public ComponentHealth checkHealth() {
                        return ComponentHealth.builder()
                                .componentName("degraded-component")
                                .status(HealthStatus.DEGRADED)
                                .details(Map.of("reason", "high load"))
                                .timestamp(Instant.now())
                                .build();
                    }
                }
        ));
    }

    @Test
    void overallStatus_isDegraded_whenAnyComponentDegraded() {
        Map<String, Object> health = healthCheckService.getHealth();
        assertEquals(HealthStatus.DEGRADED, health.get("status"));
    }

    @Test
    void overallStatus_isDown_whenAnyComponentDown() {
        healthCheckService = new HealthCheckService(List.of(
                new HealthIndicator() {
                    @Override
                    public String componentName() { return "down-component"; }

                    @Override
                    public ComponentHealth checkHealth() {
                        return ComponentHealth.builder()
                                .componentName("down-component")
                                .status(HealthStatus.DOWN)
                                .details(Map.of("error", "connection refused"))
                                .timestamp(Instant.now())
                                .build();
                    }
                }
        ));

        Map<String, Object> health = healthCheckService.getHealth();
        assertEquals(HealthStatus.DOWN, health.get("status"));
    }

    @Test
    void overallStatus_isUp_whenAllComponentsUp() {
        healthCheckService = new HealthCheckService(List.of(
                new HealthIndicator() {
                    @Override
                    public String componentName() { return "comp1"; }

                    @Override
                    public ComponentHealth checkHealth() {
                        return ComponentHealth.builder()
                                .componentName("comp1")
                                .status(HealthStatus.UP)
                                .details(Map.of())
                                .timestamp(Instant.now())
                                .build();
                    }
                }
        ));

        Map<String, Object> health = healthCheckService.getHealth();
        assertEquals(HealthStatus.UP, health.get("status"));
    }

    @Test
    void componentHealth_isDown_whenIndicatorThrowsException() {
        healthCheckService = new HealthCheckService(List.of(
                new HealthIndicator() {
                    @Override
                    public String componentName() { return "faulty"; }

                    @Override
                    public ComponentHealth checkHealth() {
                        throw new RuntimeException("unexpected error");
                    }
                }
        ));

        Map<String, Object> health = healthCheckService.getHealth();
        assertEquals(HealthStatus.DOWN, health.get("status"));

        Map<String, Object> components = (Map<String, Object>) health.get("components");
        ComponentHealth faultyHealth = (ComponentHealth) components.get("faulty");
        assertEquals(HealthStatus.DOWN, faultyHealth.getStatus());
    }

    @Test
    void healthResponse_containsTimestamp() {
        Map<String, Object> health = healthCheckService.getHealth();
        assertNotNull(health.get("timestamp"));
    }

    @Test
    void healthResponse_containsAllComponents() {
        Map<String, Object> health = healthCheckService.getHealth();
        Map<String, Object> components = (Map<String, Object>) health.get("components");
        assertTrue(components.containsKey("up-component"));
        assertTrue(components.containsKey("degraded-component"));
    }
}
