package com.example.exchangerate.watchlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistEntry createEntry(WatchlistEntry entry) {
        WatchlistEntry saved = watchlistRepository.save(entry);
        log.info("Watchlist entry created: id={} {}->{}", saved.getId(), saved.getFromCurrency(), saved.getToCurrency());
        return saved;
    }

    public Optional<WatchlistEntry> getEntry(String id) {
        return watchlistRepository.findById(id);
    }

    public List<WatchlistEntry> getAllEntries() {
        return watchlistRepository.findAll();
    }

    public List<WatchlistEntry> getEntriesByPair(String from, String to) {
        return watchlistRepository.findByCurrencyPair(from, to);
    }

    public List<WatchlistEntry> getEntriesByPriority(String priority) {
        return watchlistRepository.findByPriority(priority);
    }

    public List<WatchlistEntry> getEnabledEntries() {
        return watchlistRepository.findEnabled();
    }

    public boolean deleteEntry(String id) {
        boolean deleted = watchlistRepository.deleteById(id);
        if (deleted) {
            log.info("Watchlist entry deleted: id={}", id);
        }
        return deleted;
    }

    public WatchlistEntry updateEntry(String id, WatchlistEntry updated) {
        return watchlistRepository.findById(id)
                .map(existing -> {
                    WatchlistEntry merged = WatchlistEntry.builder()
                            .id(existing.getId())
                            .fromCurrency(updated.getFromCurrency() != null ? updated.getFromCurrency() : existing.getFromCurrency())
                            .toCurrency(updated.getToCurrency() != null ? updated.getToCurrency() : existing.getToCurrency())
                            .label(updated.getLabel() != null ? updated.getLabel() : existing.getLabel())
                            .priority(updated.getPriority() != null ? updated.getPriority() : existing.getPriority())
                            .enabled(updated.isEnabled())
                            .createdAt(existing.getCreatedAt())
                            .updatedAt(Instant.now())
                            .build();
                    WatchlistEntry saved = watchlistRepository.save(merged);
                    log.info("Watchlist entry updated: id={}", id);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found: " + id));
    }

    public WatchlistEntry toggleEnabled(String id) {
        return watchlistRepository.findById(id)
                .map(existing -> {
                    WatchlistEntry toggled = WatchlistEntry.builder()
                            .id(existing.getId())
                            .fromCurrency(existing.getFromCurrency())
                            .toCurrency(existing.getToCurrency())
                            .label(existing.getLabel())
                            .priority(existing.getPriority())
                            .enabled(!existing.isEnabled())
                            .createdAt(existing.getCreatedAt())
                            .updatedAt(Instant.now())
                            .build();
                    WatchlistEntry saved = watchlistRepository.save(toggled);
                    log.info("Watchlist entry toggled: id={} enabled={}", id, saved.isEnabled());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Watchlist entry not found: " + id));
    }

    public long getEntryCount() {
        return watchlistRepository.count();
    }
}
