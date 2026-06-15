package com.example.exchangerate.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class CacheMetricsCollector {

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    private final AtomicLong puts = new AtomicLong(0);

    public void recordHit() {
        hits.incrementAndGet();
    }

    public void recordMiss() {
        misses.incrementAndGet();
    }

    public void recordEviction() {
        evictions.incrementAndGet();
    }

    public void recordPut() {
        puts.incrementAndGet();
    }

    public long getHits() {
        return hits.get();
    }

    public long getMisses() {
        return misses.get();
    }

    public long getEvictions() {
        return evictions.get();
    }

    public long getPuts() {
        return puts.get();
    }

    public double getHitRate() {
        long total = hits.get() + misses.get();
        return total == 0 ? 0.0 : (double) hits.get() / total;
    }

    public void reset() {
        hits.set(0);
        misses.set(0);
        evictions.set(0);
        puts.set(0);
        log.info("Cache metrics reset");
    }
}
