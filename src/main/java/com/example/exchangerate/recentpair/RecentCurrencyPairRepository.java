package com.example.exchangerate.recentpair;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecentCurrencyPairRepository {

    private final ConcurrentHashMap<String, RecentCurrencyPair> pairs = new ConcurrentHashMap<>();

    private String key(String from, String to) {
        return from.toUpperCase() + ":" + to.toUpperCase();
    }

    public RecentCurrencyPair recordUse(String from, String to, Instant timestamp) {
        String key = key(from, to);
        String upperFrom = from.toUpperCase();
        String upperTo = to.toUpperCase();

        RecentCurrencyPair existing = pairs.get(key);
        if (existing != null) {
            RecentCurrencyPair updated = RecentCurrencyPair.builder()
                    .fromCurrency(upperFrom)
                    .toCurrency(upperTo)
                    .lastUsedAt(timestamp)
                    .useCount(existing.getUseCount() + 1)
                    .build();
            pairs.put(key, updated);
            log.debug("Recent pair use recorded: {}->{} uses={}", upperFrom, upperTo, updated.getUseCount());
            return updated;
        }

        RecentCurrencyPair created = RecentCurrencyPair.builder()
                .fromCurrency(upperFrom)
                .toCurrency(upperTo)
                .lastUsedAt(timestamp)
                .useCount(1)
                .build();
        pairs.put(key, created);
        log.debug("Recent pair created: {}->{}", upperFrom, upperTo);
        return created;
    }

    public Optional<RecentCurrencyPair> findByPair(String from, String to) {
        return Optional.ofNullable(pairs.get(key(from, to)));
    }

    public List<RecentCurrencyPair> findAllRecent() {
        return pairs.values().stream()
                .sorted(Comparator.comparing(RecentCurrencyPair::getLastUsedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<RecentCurrencyPair> findTopRecent(int limit) {
        return findAllRecent().stream().limit(Math.max(limit, 0)).collect(Collectors.toList());
    }

    public List<RecentCurrencyPair> findMostUsed(int limit) {
        return pairs.values().stream()
                .sorted(Comparator.comparingLong(RecentCurrencyPair::getUseCount).reversed())
                .limit(Math.max(limit, 0))
                .collect(Collectors.toList());
    }

    public boolean deleteByPair(String from, String to) {
        RecentCurrencyPair removed = pairs.remove(key(from, to));
        if (removed != null) {
            log.info("Recent pair deleted: {}->{}", from, to);
            return true;
        }
        return false;
    }

    public void clear() {
        pairs.clear();
        log.info("All recent pairs cleared");
    }

    public long count() {
        return pairs.size();
    }
}
