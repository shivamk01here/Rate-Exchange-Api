package com.example.exchangerate.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

    private RateLimitConfig config;
    private RateLimitService service;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setDefaultRequestsPerWindow(5);
        config.setWindowSize(Duration.ofMinutes(1));
        config.setBypassPaths(Arrays.asList("/api/health", "/api/version"));
        service = new RateLimitService(config);
    }

    @Test
    void isAllowed_withinLimit_returnsTrue() {
        assertTrue(service.isAllowed("192.168.1.1", "/api/rates"));
    }

    @Test
    void isAllowed_atLimit_returnsFalse() {
        for (int i = 0; i < 5; i++) {
            service.isAllowed("192.168.1.1", "/api/rates");
        }
        assertFalse(service.isAllowed("192.168.1.1", "/api/rates"));
    }

    @Test
    void isAllowed_differentClients_separateLimits() {
        for (int i = 0; i < 5; i++) {
            service.isAllowed("192.168.1.1", "/api/rates");
        }
        assertFalse(service.isAllowed("192.168.1.1", "/api/rates"));
        assertTrue(service.isAllowed("192.168.1.2", "/api/rates"));
    }

    @Test
    void isAllowed_differentEndpoints_separateLimits() {
        for (int i = 0; i < 5; i++) {
            service.isAllowed("192.168.1.1", "/api/rates");
        }
        assertFalse(service.isAllowed("192.168.1.1", "/api/rates"));
        assertTrue(service.isAllowed("192.168.1.1", "/api/currencies"));
    }

    @Test
    void isAllowed_bypassPath_alwaysAllowed() {
        for (int i = 0; i < 100; i++) {
            assertTrue(service.isAllowed("192.168.1.1", "/api/health"));
        }
    }

    @Test
    void isAllowed_disabled_alwaysAllowed() {
        config.setEnabled(false);
        for (int i = 0; i < 100; i++) {
            assertTrue(service.isAllowed("192.168.1.1", "/api/rates"));
        }
    }

    @Test
    void getEntry_returnsEntryAfterRequest() {
        service.isAllowed("192.168.1.1", "/api/rates");
        RateLimitEntry entry = service.getEntry("192.168.1.1", "/api/rates");
        assertNotNull(entry);
        assertEquals(1, entry.getRequestCount());
        assertEquals(5, entry.getMaxRequests());
    }

    @Test
    void getEntry_returnsNullForUnknownKey() {
        assertNull(service.getEntry("192.168.1.1", "/api/rates"));
    }

    @Test
    void clearEntries_removesAll() {
        service.isAllowed("192.168.1.1", "/api/rates");
        service.clearEntries();
        assertNull(service.getEntry("192.168.1.1", "/api/rates"));
    }

    @Test
    void customEndpointLimit_overridesDefault() {
        RateLimitConfig.EndpointLimit endpointLimit = new RateLimitConfig.EndpointLimit();
        endpointLimit.setMaxRequests(2);
        endpointLimit.setWindow(Duration.ofMinutes(1));
        config.getEndpoints().put("/api/rates", endpointLimit);

        assertTrue(service.isAllowed("192.168.1.1", "/api/rates"));
        assertTrue(service.isAllowed("192.168.1.1", "/api/rates"));
        assertFalse(service.isAllowed("192.168.1.1", "/api/rates"));
    }

    @Test
    void entryTracksRemainingRequests() {
        service.isAllowed("192.168.1.1", "/api/rates");
        RateLimitEntry entry = service.getEntry("192.168.1.1", "/api/rates");
        assertEquals(4, entry.getRemainingRequests());

        service.isAllowed("192.168.1.1", "/api/rates");
        entry = service.getEntry("192.168.1.1", "/api/rates");
        assertEquals(3, entry.getRemainingRequests());
    }

    @Test
    void entryWindowEndIsSetCorrectly() {
        service.isAllowed("192.168.1.1", "/api/rates");
        RateLimitEntry entry = service.getEntry("192.168.1.1", "/api/rates");
        assertNotNull(entry.getWindowStart());
        assertNotNull(entry.getWindowEnd());
        assertTrue(entry.getWindowEnd().isAfter(entry.getWindowStart()));
    }
}
