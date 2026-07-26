package com.example.exchangerate.controllers;

import com.example.exchangerate.history.ConversionHistoryEntry;
import com.example.exchangerate.history.ConversionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class ConversionHistoryController {

    private final ConversionHistoryService historyService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConversionHistoryEntry recordConversion(@Valid @RequestBody ConversionHistoryEntry entry) {
        log.info("Recording conversion: {}->{} amount={}", entry.getFromCurrency(),
                entry.getToCurrency(), entry.getAmount());
        return historyService.recordConversion(entry);
    }

    @GetMapping
    public List<ConversionHistoryEntry> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return historyService.getEntries(page, size);
    }

    @GetMapping("/{id}")
    public ConversionHistoryEntry getEntry(@PathVariable String id) {
        return historyService.getEntry(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "History entry not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<ConversionHistoryEntry> getEntriesByPair(
            @RequestParam String from,
            @RequestParam String to) {
        return historyService.getEntriesByCurrencyPair(from, to);
    }

    @GetMapping("/by-status")
    public List<ConversionHistoryEntry> getEntriesByStatus(@RequestParam String status) {
        return historyService.getEntriesByStatus(status);
    }

    @GetMapping("/count")
    public Map<String, Object> getCount(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (from != null && to != null) {
            return Map.of("count", historyService.getCountByCurrencyPair(from, to));
        }
        return Map.of("count", historyService.getTotalCount());
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        return historyService.getStatistics();
    }

    @GetMapping("/recent-activity")
    public Map<String, Object> getRecentActivity(
            @RequestParam(defaultValue = "24") int hours) {
        return historyService.getRecentActivity(hours);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteEntry(@PathVariable String id) {
        historyService.deleteEntry(id);
        return Map.of("status", "deleted", "id", id);
    }

    @DeleteMapping
    public Map<String, String> clearHistory() {
        historyService.clearHistory();
        return Map.of("status", "cleared");
    }
}
