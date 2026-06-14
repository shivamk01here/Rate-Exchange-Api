package com.example.exchangerate.controllers;

import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.services.RateCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final RateCacheService rateCacheService;
    private final CacheMetricsCollector cacheMetricsCollector;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
                "size", rateCacheService.size(),
                "hits", cacheMetricsCollector.getHits(),
                "misses", cacheMetricsCollector.getMisses(),
                "puts", cacheMetricsCollector.getPuts(),
                "evictions", cacheMetricsCollector.getEvictions(),
                "hitRate", String.format("%.2f", cacheMetricsCollector.getHitRate()));
    }

    @DeleteMapping("/{from}/{to}")
    public ResponseEntity<Void> evict(@PathVariable String from, @PathVariable String to) {
        rateCacheService.evict(from, to);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        rateCacheService.clearAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evict-expired")
    public Map<String, Object> evictExpired() {
        int evicted = rateCacheService.evictExpired();
        return Map.of("evicted", evicted);
    }
}
