package com.example.exchangerate.services;

import com.example.exchangerate.config.CacheConfig;
import com.example.exchangerate.models.CacheEntry;
import com.example.exchangerate.models.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateCacheService {

    private final CacheConfig cacheConfig;
    private final CacheMetricsCollector metrics;
    private final ConcurrentHashMap<String, CacheEntry<ExchangeRateResponse>> cache = new ConcurrentHashMap<>();

    public ExchangeRateResponse get(String fromCurrency, String toCurrency) {
        String key = cacheKey(fromCurrency, toCurrency);
        CacheEntry<ExchangeRateResponse> entry = cache.get(key);
        if (entry == null) {
            metrics.recordMiss();
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(key);
            metrics.recordEviction();
            log.debug("Cache entry for {} expired and removed", key);
            return null;
        }
        metrics.recordHit();
        log.debug("Cache hit for {}", key);
        return entry.getValue();
    }

    public void put(String fromCurrency, String toCurrency, ExchangeRateResponse response) {
        if (cache.size() >= cacheConfig.getMaxSize()) {
            log.warn("Cache at max size ({}), evicting oldest entries", cacheConfig.getMaxSize());
            evictOldest();
        }
        String key = cacheKey(fromCurrency, toCurrency);
        Instant now = Instant.now();
        CacheEntry<ExchangeRateResponse> entry = CacheEntry.<ExchangeRateResponse>builder()
                .value(response)
                .createdAt(now)
                .expiresAt(now.plus(cacheConfig.getTtl()))
                .build();
        cache.put(key, entry);
        metrics.recordPut();
        log.debug("Cached rate for {} with TTL {}", key, cacheConfig.getTtl());
    }

    public void evict(String fromCurrency, String toCurrency) {
        String key = cacheKey(fromCurrency, toCurrency);
        cache.remove(key);
        metrics.recordEviction();
        log.info("Evicted cache entry for {}", key);
    }

    public void clearAll() {
        int size = cache.size();
        cache.clear();
        log.info("Cleared entire cache ({} entries)", size);
    }

    public int size() {
        return cache.size();
    }

    public int evictExpired() {
        List<String> expiredKeys = new ArrayList<>();
        for (Map.Entry<String, CacheEntry<ExchangeRateResponse>> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredKeys.add(entry.getKey());
            }
        }
        expiredKeys.forEach(cache::remove);
        if (!expiredKeys.isEmpty()) {
            log.info("Evicted {} expired cache entries", expiredKeys.size());
        }
        return expiredKeys.size();
    }

    private void evictOldest() {
        cache.entrySet().stream()
                .min(Map.Entry.comparingByValue(
                        (a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt())))
                .ifPresent(entry -> cache.remove(entry.getKey()));
    }

    public static String cacheKey(String fromCurrency, String toCurrency) {
        return (fromCurrency != null ? fromCurrency.toUpperCase() : "") + "_"
                + (toCurrency != null ? toCurrency.toUpperCase() : "");
    }
}
