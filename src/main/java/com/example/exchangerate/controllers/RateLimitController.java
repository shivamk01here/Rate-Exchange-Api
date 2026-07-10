package com.example.exchangerate.controllers;

import com.example.exchangerate.ratelimit.RateLimitConfig;
import com.example.exchangerate.ratelimit.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rate-limit")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitConfig rateLimitConfig;
    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", rateLimitConfig.isEnabled());
        status.put("defaultRequestsPerWindow", rateLimitConfig.getDefaultRequestsPerWindow());
        status.put("windowSize", rateLimitConfig.getWindowSize().toString());
        status.put("bypassPaths", rateLimitConfig.getBypassPaths());
        status.put("endpoints", rateLimitConfig.getEndpoints());
        return status;
    }

    @GetMapping("/clear")
    public Map<String, String> clearAll() {
        rateLimitService.clearEntries();
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "cleared");
        response.put("message", "All rate limit entries have been cleared");
        return response;
    }
}
