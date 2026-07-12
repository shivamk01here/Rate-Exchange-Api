package com.example.exchangerate.trend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RateTrendRepository {

    private final CopyOnWriteArrayList<RateSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<RateSnapshot>> pairIndex = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public RateSnapshot save(RateSnapshot snapshot) {
        String pairKey = pairKey(snapshot.getFromCurrency(), snapshot.getToCurrency());
        snapshots.add(snapshot);

        pairIndex.computeIfAbsent(pairKey, k -> new CopyOnWriteArrayList<>()).add(snapshot);

        log.debug("RateSnapshot saved: {}->{} rate={}", snapshot.getFromCurrency(), snapshot.getToCurrency(), snapshot.getRate());
        return snapshot;
    }

    public List<RateSnapshot> findAll() {
        return new ArrayList<>(snapshots);
    }

    public List<RateSnapshot> findByCurrencyPair(String from, String to) {
        String key = pairKey(from, to);
        return pairIndex.getOrDefault(key, new CopyOnWriteArrayList<>());
    }

    public Optional<RateSnapshot> findLatestByCurrencyPair(String from, String to) {
        List<RateSnapshot> pairSnapshots = findByCurrencyPair(from, to);
        return pairSnapshots.stream()
                .max(Comparator.comparing(RateSnapshot::getTimestamp));
    }

    public List<RateSnapshot> findRecentByCurrencyPair(String from, String to, int limit) {
        return findByCurrencyPair(from, to).stream()
                .sorted(Comparator.comparing(RateSnapshot::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long count() {
        return snapshots.size();
    }

    public long countByPair(String from, String to) {
        return findByCurrencyPair(from, to).size();
    }

    public Map<String, Long> getPairCounts() {
        return pairIndex.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue().size()));
    }

    public void deleteByCurrencyPair(String from, String to) {
        String key = pairKey(from, to);
        pairIndex.remove(key);
        snapshots.removeIf(s -> pairKey(s.getFromCurrency(), s.getToCurrency()).equals(key));
        log.info("Deleted all snapshots for {}->{}", from, to);
    }

    public void clear() {
        snapshots.clear();
        pairIndex.clear();
        log.info("All rate snapshots cleared");
    }

    private String pairKey(String from, String to) {
        return (from + "/" + to).toUpperCase();
    }
}
