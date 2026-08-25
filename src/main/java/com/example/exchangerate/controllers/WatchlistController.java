package com.example.exchangerate.controllers;

import com.example.exchangerate.watchlist.WatchlistEntry;
import com.example.exchangerate.watchlist.WatchlistService;
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
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public WatchlistEntry createEntry(@Valid @RequestBody WatchlistEntry entry) {
        log.info("Creating watchlist entry: {}->{}", entry.getFromCurrency(), entry.getToCurrency());
        return watchlistService.createEntry(entry);
    }

    @GetMapping
    public List<WatchlistEntry> getAllEntries() {
        return watchlistService.getAllEntries();
    }

    @GetMapping("/{id}")
    public WatchlistEntry getEntry(@PathVariable String id) {
        return watchlistService.getEntry(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watchlist entry not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<WatchlistEntry> getEntriesByPair(@RequestParam String from, @RequestParam String to) {
        return watchlistService.getEntriesByPair(from, to);
    }

    @GetMapping("/by-priority")
    public List<WatchlistEntry> getEntriesByPriority(@RequestParam String priority) {
        return watchlistService.getEntriesByPriority(priority);
    }

    @GetMapping("/enabled")
    public List<WatchlistEntry> getEnabledEntries() {
        return watchlistService.getEnabledEntries();
    }

    @PutMapping("/{id}")
    public WatchlistEntry updateEntry(@PathVariable String id, @Valid @RequestBody WatchlistEntry entry) {
        try {
            return watchlistService.updateEntry(id, entry);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteEntry(@PathVariable String id) {
        boolean deleted = watchlistService.deleteEntry(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Watchlist entry not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PatchMapping("/{id}/toggle")
    public WatchlistEntry toggleEntry(@PathVariable String id) {
        try {
            return watchlistService.toggleEnabled(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/count")
    public Map<String, Object> getEntryCount() {
        return Map.of("count", watchlistService.getEntryCount());
    }
}
