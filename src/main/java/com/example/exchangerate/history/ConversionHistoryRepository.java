package com.example.exchangerate.history;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConversionHistoryRepository {

    private final ConcurrentHashMap<String, ConversionHistoryEntry> entries = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<ConversionHistoryEntry> entryList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public ConversionHistoryEntry save(ConversionHistoryEntry entry) {
        String id = entry.getId() != null ? entry.getId() : String.valueOf(idCounter.incrementAndGet());
        ConversionHistoryEntry stored = ConversionHistoryEntry.builder()
                .id(id)
                .fromCurrency(entry.getFromCurrency())
                .toCurrency(entry.getToCurrency())
                .amount(entry.getAmount())
                .rate(entry.getRate())
                .convertedAmount(entry.getConvertedAmount())
                .provider(entry.getProvider())
                .status(entry.getStatus())
                .timestamp(entry.getTimestamp() != null ? entry.getTimestamp() : java.time.Instant.now())
                .clientIp(entry.getClientIp())
                .userAgent(entry.getUserAgent())
                .build();

        entries.put(id, stored);
        entryList.add(stored);

        log.debug("ConversionHistory saved: id={} {}->{} amount={}",
                id, stored.getFromCurrency(), stored.getToCurrency(), stored.getAmount());
        return stored;
    }

    public Optional<ConversionHistoryEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    public List<ConversionHistoryEntry> findAll() {
        return new ArrayList<>(entryList);
    }

    public List<ConversionHistoryEntry> findAll(int page, int size) {
        List<ConversionHistoryEntry> sorted = entryList.stream()
                .sorted(Comparator.comparing(ConversionHistoryEntry::getTimestamp).reversed())
                .collect(Collectors.toList());
        int start = page * size;
        if (start >= sorted.size()) {
            return new ArrayList<>();
        }
        int end = Math.min(start + size, sorted.size());
        return new ArrayList<>(sorted.subList(start, end));
    }

    public List<ConversionHistoryEntry> findByCurrencyPair(String from, String to) {
        return entryList.stream()
                .filter(e -> from.equalsIgnoreCase(e.getFromCurrency())
                        && to.equalsIgnoreCase(e.getToCurrency()))
                .sorted(Comparator.comparing(ConversionHistoryEntry::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<ConversionHistoryEntry> findByStatus(String status) {
        return entryList.stream()
                .filter(e -> status.equalsIgnoreCase(e.getStatus()))
                .sorted(Comparator.comparing(ConversionHistoryEntry::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public long count() {
        return entries.size();
    }

    public long countByCurrencyPair(String from, String to) {
        return entryList.stream()
                .filter(e -> from.equalsIgnoreCase(e.getFromCurrency())
                        && to.equalsIgnoreCase(e.getToCurrency()))
                .count();
    }

    public void deleteById(String id) {
        ConversionHistoryEntry removed = entries.remove(id);
        if (removed != null) {
            entryList.remove(removed);
            log.info("ConversionHistory deleted: id={}", id);
        }
    }

    public void clearAll() {
        entries.clear();
        entryList.clear();
        log.info("ConversionHistory cleared");
    }
}
