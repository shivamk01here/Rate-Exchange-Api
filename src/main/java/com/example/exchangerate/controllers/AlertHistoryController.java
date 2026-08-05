package com.example.exchangerate.controllers;

import com.example.exchangerate.alerthistory.AlertHistoryEntry;
import com.example.exchangerate.alerthistory.AlertHistoryService;
import com.example.exchangerate.alerthistory.AlertHistoryStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/alerts/history")
@RequiredArgsConstructor
public class AlertHistoryController {

    private final AlertHistoryService alertHistoryService;

    @GetMapping
    public List<AlertHistoryEntry> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return alertHistoryService.getEntries(page, size);
    }

    @GetMapping("/{id}")
    public AlertHistoryEntry getEntry(@PathVariable String id) {
        return alertHistoryService.getEntry(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Alert history entry not found: " + id));
    }

    @GetMapping("/by-alert")
    public List<AlertHistoryEntry> getEntriesByAlertId(@RequestParam String alertId) {
        return alertHistoryService.getEntriesByAlertId(alertId);
    }

    @GetMapping("/by-pair")
    public List<AlertHistoryEntry> getEntriesByPair(
            @RequestParam String from,
            @RequestParam String to) {
        return alertHistoryService.getEntriesByCurrencyPair(from, to);
    }

    @GetMapping("/count")
    public Map<String, Object> getCount(
            @RequestParam(required = false) String alertId) {
        if (alertId != null) {
            return Map.of("count", alertHistoryService.getCountByAlertId(alertId));
        }
        return Map.of("count", alertHistoryService.getTotalCount());
    }

    @GetMapping("/stats")
    public AlertHistoryStats getStats() {
        return alertHistoryService.getStats();
    }

    @GetMapping("/recent")
    public List<AlertHistoryEntry> getRecentTriggers(
            @RequestParam(defaultValue = "0") int hours) {
        return alertHistoryService.getRecentTriggers(hours);
    }

    @GetMapping("/top-alerts")
    public List<Map.Entry<String, Long>> getTopAlerts(
            @RequestParam(defaultValue = "0") int limit) {
        return alertHistoryService.getTopAlerts(limit);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteEntry(@PathVariable String id) {
        alertHistoryService.deleteEntry(id);
        return Map.of("status", "deleted", "id", id);
    }

    @DeleteMapping
    public Map<String, String> clearHistory() {
        alertHistoryService.clearHistory();
        return Map.of("status", "cleared");
    }
}
