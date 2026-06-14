package com.example.exchangerate;

import com.example.exchangerate.config.CacheConfig;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.services.RateCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateCacheServiceTest {

    private CacheConfig cacheConfig;
    private RateCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
        cacheConfig.setTtl(Duration.ofMinutes(5));
        cacheConfig.setMaxSize(100);
        cacheService = new RateCacheService(cacheConfig, new CacheMetricsCollector());
    }

    @Test
    void cacheHit_returnsCachedResponse() {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.50"))
                .convertedAmount(new BigDecimal("4175.00"))
                .status("SUCCESS")
                .build();

        cacheService.put("USD", "INR", response);
        ExchangeRateResponse cached = cacheService.get("USD", "INR");

        assertNotNull(cached);
        assertEquals(new BigDecimal("83.50"), cached.getRate());
    }

    @Test
    void cacheMiss_returnsNull() {
        ExchangeRateResponse cached = cacheService.get("USD", "EUR");
        assertNull(cached);
    }

    @Test
    void evict_removesEntry() {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92"))
                .build();

        cacheService.put("USD", "EUR", response);
        assertNotNull(cacheService.get("USD", "EUR"));

        cacheService.evict("USD", "EUR");
        assertNull(cacheService.get("USD", "EUR"));
    }

    @Test
    void clearAll_removesAllEntries() {
        cacheService.put("USD", "INR", ExchangeRateResponse.builder().build());
        cacheService.put("EUR", "USD", ExchangeRateResponse.builder().build());

        assertEquals(2, cacheService.size());
        cacheService.clearAll();
        assertEquals(0, cacheService.size());
    }

    @Test
    void evictExpired_removesOnlyExpiredEntries() {
        cacheConfig.setTtl(Duration.ofMillis(-1));

        ExchangeRateResponse fresh = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.50"))
                .build();
        cacheService.put("USD", "INR", fresh);

        ExchangeRateResponse stale = ExchangeRateResponse.builder()
                .fromCurrency("EUR").toCurrency("USD")
                .rate(new BigDecimal("1.08"))
                .build();
        cacheService.put("EUR", "USD", stale);

        assertEquals(2, cacheService.size());

        int evicted = cacheService.evictExpired();
        assertEquals(2, evicted);
        assertEquals(0, cacheService.size());
    }

    @Test
    void cacheKey_isCaseInsensitive() {
        String key1 = RateCacheService.cacheKey("usd", "inr");
        String key2 = RateCacheService.cacheKey("USD", "INR");
        assertEquals(key1, key2);
    }
}
