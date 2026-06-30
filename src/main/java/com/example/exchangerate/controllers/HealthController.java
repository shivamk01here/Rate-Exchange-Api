package com.example.exchangerate.controllers;

import com.example.exchangerate.health.HealthCheckService;
import com.example.exchangerate.services.ProviderMetricsCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckService healthCheckService;
    private final ProviderMetricsCollector providerMetrics;

    @GetMapping
    public Map<String, Object> health() {
        return healthCheckService.getHealth();
    }

    @GetMapping("/providers")
    public Map<String, Object> providerStats() {
        return providerMetrics.getAllStats();
    }
}
