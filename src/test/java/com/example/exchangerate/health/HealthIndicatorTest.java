package com.example.exchangerate.health;

import com.example.exchangerate.audit.AuditRepository;
import com.example.exchangerate.config.AuditConfig;
import com.example.exchangerate.config.CacheConfig;
import com.example.exchangerate.providers.ProviderFactory;
import com.example.exchangerate.services.AuditService;
import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.services.RateCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HealthIndicatorTest {

    private CacheHealthIndicator cacheHealthIndicator;
    private AuditHealthIndicator auditHealthIndicator;
    private ProviderFactory providerFactory;

    @BeforeEach
    void setUp() {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setTtl(Duration.ofMinutes(5));
        cacheConfig.setMaxSize(100);
        RateCacheService rateCacheService = new RateCacheService(cacheConfig, new CacheMetricsCollector());
        cacheHealthIndicator = new CacheHealthIndicator(rateCacheService, new CacheMetricsCollector(), cacheConfig);

        AuditRepository auditRepository = new AuditRepository();
        AuditConfig auditConfig = new AuditConfig();
        AuditService auditService = new AuditService(auditRepository, auditConfig);
        auditHealthIndicator = new AuditHealthIndicator(auditService);

        providerFactory = new ProviderFactory();
    }

    @Test
    void cacheHealthIndicator_returnsComponentName() {
        assertEquals("cache", cacheHealthIndicator.componentName());
    }

    @Test
    void cacheHealthIndicator_returnsUpStatus() {
        ComponentHealth health = cacheHealthIndicator.checkHealth();
        assertEquals(HealthStatus.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("size"));
        assertTrue(health.getDetails().containsKey("maxSize"));
        assertNotNull(health.getTimestamp());
    }

    @Test
    void auditHealthIndicator_returnsComponentName() {
        assertEquals("audit", auditHealthIndicator.componentName());
    }

    @Test
    void auditHealthIndicator_returnsUpStatus() {
        ComponentHealth health = auditHealthIndicator.checkHealth();
        assertEquals(HealthStatus.UP, health.getStatus());
        assertTrue(health.getDetails().containsKey("totalConversions"));
        assertNotNull(health.getTimestamp());
    }

    @Test
    void providerHealthIndicator_returnsComponentName() {
        ProviderHealthIndicator indicator = new ProviderHealthIndicator(providerFactory);
        assertEquals("providers", indicator.componentName());
    }

    @Test
    void providerHealthIndicator_returnsDegradedWhenNoProviders() {
        ProviderHealthIndicator indicator = new ProviderHealthIndicator(providerFactory);
        ComponentHealth health = indicator.checkHealth();
        assertEquals(HealthStatus.DEGRADED, health.getStatus());
    }
}
