package com.example.exchangerate.health;

import com.example.exchangerate.services.CurrencyCacheService;
import com.example.exchangerate.services.CurrencyMetricsCollector;
import com.example.exchangerate.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyHealthIndicatorTest {

    private CurrencyHealthIndicator currencyHealthIndicator;

    @BeforeEach
    void setUp() {
        CurrencyService currencyService = new CurrencyService();
        CurrencyMetricsCollector metrics = new CurrencyMetricsCollector();
        CurrencyCacheService cacheService = new CurrencyCacheService(currencyService, metrics);
        currencyHealthIndicator = new CurrencyHealthIndicator(cacheService, metrics);
    }

    @Test
    void componentName_returnsCurrency() {
        assertEquals("currency", currencyHealthIndicator.componentName());
    }

    @Test
    void checkHealth_returnsUpStatus() {
        ComponentHealth health = currencyHealthIndicator.checkHealth();
        assertEquals(HealthStatus.UP, health.getStatus());
        assertNotNull(health.getTimestamp());
    }

    @Test
    void checkHealth_containsSupportedCurrencies() {
        ComponentHealth health = currencyHealthIndicator.checkHealth();
        assertTrue(health.getDetails().containsKey("supportedCurrencies"));
        assertEquals(20, health.getDetails().get("supportedCurrencies"));
    }

    @Test
    void checkHealth_containsMetrics() {
        ComponentHealth health = currencyHealthIndicator.checkHealth();
        assertTrue(health.getDetails().containsKey("totalLookups"));
        assertTrue(health.getDetails().containsKey("cacheHits"));
        assertTrue(health.getDetails().containsKey("cacheMisses"));
        assertTrue(health.getDetails().containsKey("cacheHitRate"));
    }

    @Test
    void checkHealth_initialMetricsAreZero() {
        ComponentHealth health = currencyHealthIndicator.checkHealth();
        assertEquals(0L, health.getDetails().get("totalLookups"));
        assertEquals(0L, health.getDetails().get("cacheHits"));
        assertEquals(0L, health.getDetails().get("cacheMisses"));
    }
}
