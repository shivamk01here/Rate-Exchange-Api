package com.example.exchangerate.favorites;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class FavoritesMetricsCollector {

    private final AtomicLong createCount = new AtomicLong(0);
    private final AtomicLong deleteCount = new AtomicLong(0);
    private final AtomicLong updateCount = new AtomicLong(0);
    private final AtomicLong fetchCount = new AtomicLong(0);
    private final AtomicLong bulkRateFetchCount = new AtomicLong(0);

    public void recordCreate() {
        createCount.incrementAndGet();
    }

    public void recordDelete() {
        deleteCount.incrementAndGet();
    }

    public void recordUpdate() {
        updateCount.incrementAndGet();
    }

    public void recordFetch() {
        fetchCount.incrementAndGet();
    }

    public void recordBulkRateFetch() {
        bulkRateFetchCount.incrementAndGet();
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "creates", createCount.get(),
                "deletes", deleteCount.get(),
                "updates", updateCount.get(),
                "fetches", fetchCount.get(),
                "bulkRateFetches", bulkRateFetchCount.get()
        );
    }

    public void reset() {
        createCount.set(0);
        deleteCount.set(0);
        updateCount.set(0);
        fetchCount.set(0);
        bulkRateFetchCount.set(0);
        log.info("Favorites metrics reset");
    }
}
