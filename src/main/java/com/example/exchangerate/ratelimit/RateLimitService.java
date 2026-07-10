package com.example.exchangerate.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitConfig config;
    private final ConcurrentHashMap<String, RateLimitEntry> entries = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientKey, String endpoint) {
        if (!config.isEnabled()) {
            return true;
        }

        if (isBypassPath(endpoint)) {
            return true;
        }

        String entryKey = clientKey + ":" + endpoint;
        Instant now = Instant.now();
        Duration window = getWindowForEndpoint(endpoint);
        int maxRequests = getMaxRequestsForEndpoint(endpoint);

        RateLimitEntry entry = entries.compute(entryKey, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return RateLimitEntry.builder()
                        .clientKey(clientKey)
                        .endpoint(endpoint)
                        .requestCount(1)
                        .maxRequests(maxRequests)
                        .windowStart(now)
                        .windowEnd(now.plus(window))
                        .build();
            }
            existing.setRequestCount(existing.getRequestCount() + 1);
            return existing;
        });

        boolean allowed = entry.getRequestCount() <= entry.getMaxRequests();

        if (!allowed) {
            log.warn("Rate limit exceeded for client={} endpoint={} count={}/{}",
                    clientKey, endpoint, entry.getRequestCount(), entry.getMaxRequests());
        }

        return allowed;
    }

    public RateLimitEntry getEntry(String clientKey, String endpoint) {
        String entryKey = clientKey + ":" + endpoint;
        return entries.get(entryKey);
    }

    public void clearEntries() {
        entries.clear();
    }

    private boolean isBypassPath(String endpoint) {
        return config.getBypassPaths().stream()
                .anyMatch(endpoint::startsWith);
    }

    private Duration getWindowForEndpoint(String endpoint) {
        RateLimitConfig.EndpointLimit limit = config.getEndpoints().get(endpoint);
        return limit != null && limit.getWindow() != null ? limit.getWindow() : config.getWindowSize();
    }

    private int getMaxRequestsForEndpoint(String endpoint) {
        RateLimitConfig.EndpointLimit limit = config.getEndpoints().get(endpoint);
        return limit != null ? limit.getMaxRequests() : config.getDefaultRequestsPerWindow();
    }
}
