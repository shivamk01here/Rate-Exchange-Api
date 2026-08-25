package com.example.exchangerate.watchlist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WatchlistRepository {

    private final ConcurrentHashMap<String, WatchlistEntry> entries = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<WatchlistEntry> entryList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public WatchlistEntry save(WatchlistEntry entry) {
        String id = entry.getId() != null ? entry.getId() : String.valueOf(idCounter.incrementAndGet());
        WatchlistEntry stored = WatchlistEntry.builder()
                .id(id)
                .fromCurrency(entry.getFromCurrency())
                .toCurrency(entry.getToCurrency())
                .label(entry.getLabel())
                .priority(entry.getPriority())
                .enabled(entry.isEnabled())
                .createdAt(entry.getCreatedAt() != null ? entry.getCreatedAt() : java.time.Instant.now())
                .updatedAt(entry.getUpdatedAt())
                .build();

        if (entries.putIfAbsent(id, stored) == null) {
            entryList.add(stored);
        } else {
            entries.put(id, stored);
            for (int i = 0; i < entryList.size(); i++) {
                if (id.equals(entryList.get(i).getId())) {
                    entryList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("WatchlistEntry saved: id={} {}->{}", id, stored.getFromCurrency(), stored.getToCurrency());
        return stored;
    }

    public Optional<WatchlistEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public List<WatchlistEntry> findAll() {
        return new ArrayList<>(entryList);
    }

    public List<WatchlistEntry> findByCurrencyPair(String from, String to) {
        return entryList.stream()
                .filter(e -> from.equalsIgnoreCase(e.getFromCurrency())
                        && to.equalsIgnoreCase(e.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<WatchlistEntry> findByPriority(WatchlistPriority priority) {
        return entryList.stream()
                .filter(e -> e.getPriority() == priority)
                .collect(Collectors.toList());
    }

    public List<WatchlistEntry> findEnabled() {
        return entryList.stream()
                .filter(WatchlistEntry::isEnabled)
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        WatchlistEntry removed = entries.remove(id);
        if (removed != null) {
            entryList.remove(removed);
            log.info("WatchlistEntry deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return entries.size();
    }
}
