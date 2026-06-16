package com.example.exchangerate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class CurrencyMetricsCollector {

    private final AtomicLong totalLookups = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public void recordLookup() {
        totalLookups.incrementAndGet();
    }

    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public long getTotalLookups() {
        return totalLookups.get();
    }

    public long getCacheHits() {
        return cacheHits.get();
    }

    public long getCacheMisses() {
        return cacheMisses.get();
    }

    public double getCacheHitRate() {
        long total = cacheHits.get() + cacheMisses.get();
        return total == 0 ? 0.0 : (double) cacheHits.get() / total;
    }

    public void reset() {
        totalLookups.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        log.info("Currency metrics reset");
    }
}
