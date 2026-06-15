package com.example.exchangerate.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        HealthCheckService healthCheckService = new HealthCheckService(List.of(
                new HealthIndicator() {
                    @Override
                    public String componentName() { return "test-component"; }

                    @Override
                    public ComponentHealth checkHealth() {
                        return ComponentHealth.builder()
                                .componentName("test-component")
                                .status(HealthStatus.UP)
                                .details(Map.of("info", "all good"))
                                .timestamp(Instant.now())
                                .build();
                    }
                }
        ));
        healthController = new HealthController(healthCheckService);
    }

    @Test
    void healthEndpoint_returnsOverallStatus() {
        Map<String, Object> result = healthController.health();
        assertNotNull(result);
        assertEquals(HealthStatus.UP, result.get("status"));
    }

    @Test
    void healthEndpoint_containsTimestamp() {
        Map<String, Object> result = healthController.health();
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void healthEndpoint_containsComponents() {
        Map<String, Object> result = healthController.health();
        assertTrue(result.containsKey("components"));

        Map<String, Object> components = (Map<String, Object>) result.get("components");
        assertTrue(components.containsKey("test-component"));
    }

    @Test
    void healthEndpoint_componentHasCorrectShape() {
        Map<String, Object> result = healthController.health();
        Map<String, Object> components = (Map<String, Object>) result.get("components");
        ComponentHealth health = (ComponentHealth) components.get("test-component");

        assertEquals("test-component", health.getComponentName());
        assertEquals(HealthStatus.UP, health.getStatus());
        assertNotNull(health.getTimestamp());
    }
}
