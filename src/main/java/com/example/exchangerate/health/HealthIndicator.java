package com.example.exchangerate.health;

public interface HealthIndicator {
    String componentName();
    ComponentHealth checkHealth();
}
