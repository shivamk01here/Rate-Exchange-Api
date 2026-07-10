package com.example.exchangerate.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties("rate-limit")
public class RateLimitConfig {

    private boolean enabled = true;
    private int defaultRequestsPerWindow = 100;
    private Duration windowSize = Duration.ofMinutes(1);
    private List<String> bypassPaths = new ArrayList<>();
    private Map<String, EndpointLimit> endpoints = new HashMap<>();

    @Data
    public static class EndpointLimit {
        private int maxRequests = 100;
        private Duration window = Duration.ofMinutes(1);
    }
}
