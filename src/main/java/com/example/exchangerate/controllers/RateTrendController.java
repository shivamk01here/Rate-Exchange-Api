package com.example.exchangerate.controllers;

import com.example.exchangerate.trend.RateSnapshot;
import com.example.exchangerate.trend.RateTrend;
import com.example.exchangerate.trend.RateTrendService;
import com.example.exchangerate.trend.TrendSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
public class RateTrendController {

    private final RateTrendService rateTrendService;

    @GetMapping("/snapshots")
    public List<RateSnapshot> getSnapshots(@RequestParam String from, @RequestParam String to) {
        log.info("Fetching snapshots for {}->{}", from, to);
        return rateTrendService.getSnapshots(from.toUpperCase(), to.toUpperCase());
    }

    @GetMapping("/snapshots/latest")
    public RateSnapshot getLatestSnapshot(@RequestParam String from, @RequestParam String to) {
        return rateTrendService.getLatestSnapshot(from.toUpperCase(), to.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No snapshots found for " + from + "->" + to));
    }

    @GetMapping
    public List<RateTrend> getRecentTrends(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching recent trends for {}->{} limit={}", from, to, limit);
        return rateTrendService.getRecentTrends(from.toUpperCase(), to.toUpperCase(), limit);
    }

    @GetMapping("/summary")
    public TrendSummary getTrendSummary(@RequestParam String from, @RequestParam String to) {
        log.info("Fetching trend summary for {}->{}", from, to);
        return rateTrendService.getTrendSummary(from.toUpperCase(), to.toUpperCase());
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        RateTrendService.MapStats stats = rateTrendService.getStats();
        return Map.of(
                "totalSnapshots", stats.totalSnapshots(),
                "pairCounts", stats.pairCounts()
        );
    }

    @DeleteMapping("/clear")
    public Map<String, String> clearAll() {
        rateTrendService.clearAll();
        return Map.of("status", "cleared");
    }

    @DeleteMapping("/clear/{from}/{to}")
    public Map<String, String> clearByPair(@PathVariable String from, @PathVariable String to) {
        rateTrendService.clearByPair(from.toUpperCase(), to.toUpperCase());
        return Map.of("status", "cleared", "from", from.toUpperCase(), "to", to.toUpperCase());
    }
}
