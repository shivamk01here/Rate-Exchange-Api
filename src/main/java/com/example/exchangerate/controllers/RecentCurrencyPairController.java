package com.example.exchangerate.controllers;

import com.example.exchangerate.recentpair.RecentCurrencyPair;
import com.example.exchangerate.recentpair.RecentCurrencyPairService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/recent-pairs")
@RequiredArgsConstructor
public class RecentCurrencyPairController {

    private final RecentCurrencyPairService pairService;

    @PostMapping("/record")
    public RecentCurrencyPair recordPair(@RequestParam String from, @RequestParam String to) {
        try {
            return pairService.recordPair(from, to);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public List<RecentCurrencyPair> getRecentPairs() {
        return pairService.getRecentPairs();
    }

    @GetMapping("/by-pair")
    public RecentCurrencyPair getPair(@RequestParam String from, @RequestParam String to) {
        return pairService.getPair(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pair not found: " + from + "->" + to));
    }

    @GetMapping("/top")
    public List<RecentCurrencyPair> getTopRecent(@RequestParam(defaultValue = "10") int limit) {
        return pairService.getTopRecent(limit);
    }

    @GetMapping("/most-used")
    public List<RecentCurrencyPair> getMostUsed(@RequestParam(defaultValue = "10") int limit) {
        return pairService.getMostUsed(limit);
    }

    @DeleteMapping("/by-pair")
    public Map<String, String> deletePair(@RequestParam String from, @RequestParam String to) {
        boolean deleted = pairService.deletePair(from, to);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pair not found: " + from + "->" + to);
        }
        return Map.of("status", "deleted", "from", from, "to", to);
    }

    @DeleteMapping("/clear")
    public Map<String, String> clearAll() {
        pairService.clearAll();
        return Map.of("status", "cleared");
    }

    @GetMapping("/count")
    public Map<String, Object> getPairCount() {
        return Map.of("count", pairService.getPairCount());
    }
}
