package com.example.exchangerate.services;

import com.example.exchangerate.models.ProviderCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ProviderMetricsCollector {

    private final ConcurrentHashMap<ProviderCodes, AtomicLong> callCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ProviderCodes, AtomicLong> successCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ProviderCodes, AtomicLong> failureCounts = new ConcurrentHashMap<>();

    public void recordCall(ProviderCodes code) {
        callCounts.computeIfAbsent(code, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordSuccess(ProviderCodes code) {
        successCounts.computeIfAbsent(code, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordFailure(ProviderCodes code) {
        failureCounts.computeIfAbsent(code, k -> new AtomicLong()).incrementAndGet();
    }

    public Map<String, Object> getProviderStats(ProviderCodes code) {
        long calls = callCounts.getOrDefault(code, new AtomicLong()).get();
        long successes = successCounts.getOrDefault(code, new AtomicLong()).get();
        long failures = failureCounts.getOrDefault(code, new AtomicLong()).get();
        return Map.of(
                "provider", code.name(),
                "totalCalls", calls,
                "successes", successes,
                "failures", failures);
    }

    public Map<String, Object> getAllStats() {
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        for (ProviderCodes code : ProviderCodes.values()) {
            stats.put(code.name(), getProviderStats(code));
        }
        return stats;
    }

    public void reset() {
        callCounts.clear();
        successCounts.clear();
        failureCounts.clear();
        log.info("Provider metrics reset");
    }
}
