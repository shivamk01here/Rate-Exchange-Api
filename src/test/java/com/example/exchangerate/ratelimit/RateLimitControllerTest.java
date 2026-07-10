package com.example.exchangerate.controllers;

import com.example.exchangerate.ratelimit.RateLimitConfig;
import com.example.exchangerate.ratelimit.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitControllerTest {

    private RateLimitController controller;
    private RateLimitConfig config;
    private RateLimitService service;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setDefaultRequestsPerWindow(100);
        config.setWindowSize(Duration.ofMinutes(1));
        config.setBypassPaths(Arrays.asList("/api/health"));
        service = new RateLimitService(config);
        controller = new RateLimitController(config, service);
    }

    @Test
    void getStatus_returnsConfigDetails() {
        @SuppressWarnings("unchecked")
        Map<String, Object> status = controller.getStatus();

        assertEquals(true, status.get("enabled"));
        assertEquals(100, status.get("defaultRequestsPerWindow"));
        assertNotNull(status.get("windowSize"));
        assertNotNull(status.get("bypassPaths"));
        assertNotNull(status.get("endpoints"));
    }

    @Test
    void getStatus_disabledConfig() {
        config.setEnabled(false);

        @SuppressWarnings("unchecked")
        Map<String, Object> status = controller.getStatus();

        assertEquals(false, status.get("enabled"));
    }

    @Test
    void clearAll_returnsSuccessStatus() {
        service.isAllowed("192.168.1.1", "/api/rates");
        assertNotNull(service.getEntry("192.168.1.1", "/api/rates"));

        Map<String, String> result = controller.clearAll();

        assertEquals("cleared", result.get("status"));
        assertNotNull(result.get("message"));
    }

    @Test
    void clearAll_removesExistingEntries() {
        service.isAllowed("192.168.1.1", "/api/rates");
        service.isAllowed("10.0.0.1", "/api/currencies");

        controller.clearAll();

        assertNull(service.getEntry("192.168.1.1", "/api/rates"));
        assertNull(service.getEntry("10.0.0.1", "/api/currencies"));
    }

    @Test
    void getStatus_bypassPathsAreIncluded() {
        @SuppressWarnings("unchecked")
        Map<String, Object> status = controller.getStatus();

        @SuppressWarnings("unchecked")
        java.util.List<String> bypassPaths = (java.util.List<String>) status.get("bypassPaths");
        assertTrue(bypassPaths.contains("/api/health"));
    }
}
