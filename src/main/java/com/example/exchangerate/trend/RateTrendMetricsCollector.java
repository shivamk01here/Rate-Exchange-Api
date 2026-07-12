package com.example.exchangerate.trend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class RateTrendMetricsCollector {

    private final AtomicLong recordCount = new AtomicLong(0);
    private final AtomicLong queryCount = new AtomicLong(0);
    private final AtomicLong summaryCount = new AtomicLong(0);
    private final AtomicLong risingCount = new AtomicLong(0);
    private final AtomicLong fallingCount = new AtomicLong(0);
    private final AtomicLong stableCount = new AtomicLong(0);

    public void recordRateCapture() {
        recordCount.incrementAndGet();
    }

    public void recordQuery() {
        queryCount.incrementAndGet();
    }

    public void recordSummary() {
        summaryCount.incrementAndGet();
    }

    public void recordDirection(RateTrend.TrendDirection direction) {
        switch (direction) {
            case RISING -> risingCount.incrementAndGet();
            case FALLING -> fallingCount.incrementAndGet();
            case STABLE -> stableCount.incrementAndGet();
        }
    }

    public Map<String, Object> getStats() {
        return Map.of(
                "recordCount", recordCount.get(),
                "queryCount", queryCount.get(),
                "summaryCount", summaryCount.get(),
                "risingCount", risingCount.get(),
                "fallingCount", fallingCount.get(),
                "stableCount", stableCount.get()
        );
    }

    public void reset() {
        recordCount.set(0);
        queryCount.set(0);
        summaryCount.set(0);
        risingCount.set(0);
        fallingCount.set(0);
        stableCount.set(0);
        log.info("Rate trend metrics reset");
    }
}
