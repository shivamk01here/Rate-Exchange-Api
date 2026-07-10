package com.example.exchangerate.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class RateLimitEntry {

    private String clientKey;
    private String endpoint;
    private int requestCount;
    private int maxRequests;
    private Instant windowStart;
    private Instant windowEnd;

    public boolean isExpired() {
        return Instant.now().isAfter(windowEnd);
    }

    public int getRemainingRequests() {
        return Math.max(0, maxRequests - requestCount);
    }
}
