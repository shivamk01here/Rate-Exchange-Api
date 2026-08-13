package com.example.exchangerate.controllers;

import com.example.exchangerate.trending.TrendingPair;
import com.example.exchangerate.trending.TrendingPairService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/trending-pairs")
@RequiredArgsConstructor
public class TrendingPairController {

    private final TrendingPairService trendingPairService;

    @GetMapping
    public List<TrendingPair> getTrendingPairs(@RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching trending pairs by volume limit={}", limit);
        return trendingPairService.getTrendingPairs(limit);
    }

    @GetMapping("/by-count")
    public List<TrendingPair> getTrendingPairsByCount(@RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching trending pairs by count limit={}", limit);
        return trendingPairService.getTrendingPairsByCount(limit);
    }

    @GetMapping("/recent")
    public List<TrendingPair> getTrendingPairsSince(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching trending pairs since {}h limit={}", hours, limit);
        return trendingPairService.getTrendingPairsSince(hours, limit);
    }

    @GetMapping("/count")
    public Map<String, Object> getDistinctPairCount() {
        return Map.of("count", trendingPairService.getDistinctPairCount());
    }
}
