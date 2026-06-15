package com.example.exchangerate.config;

import com.example.exchangerate.services.RateCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionScheduler {

    private final RateCacheService rateCacheService;

    @Scheduled(fixedRateString = "${cache.eviction-interval-ms:60000}")
    public void evictExpiredEntries() {
        int evicted = rateCacheService.evictExpired();
        if (evicted > 0) {
            log.info("Scheduled eviction removed {} expired cache entries", evicted);
        }
    }
}
