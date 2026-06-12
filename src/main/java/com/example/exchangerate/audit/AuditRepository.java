package com.example.exchangerate.audit;

import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ProviderCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuditRepository {

    private final List<ConversionRecord> records = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);
    private final Map<String, List<ConversionRecord>> currencyPairIndex = new ConcurrentHashMap<>();

    public ConversionRecord save(ConversionRecord record) {
        String id = String.valueOf(idCounter.incrementAndGet());
        ConversionRecord stored = ConversionRecord.builder()
                .id(id)
                .providerCode(record.getProviderCode())
                .fromCurrency(record.getFromCurrency())
                .toCurrency(record.getToCurrency())
                .amount(record.getAmount())
                .rate(record.getRate())
                .convertedAmount(record.getConvertedAmount())
                .status(record.getStatus())
                .timestamp(record.getTimestamp() != null ? record.getTimestamp() : Instant.now())
                .build();

        records.add(stored);
        String pairKey = pairKey(stored.getFromCurrency(), stored.getToCurrency());
        currencyPairIndex.computeIfAbsent(pairKey, k -> new CopyOnWriteArrayList<>()).add(stored);

        log.debug("Audit record saved: id={} {}->{} status={}", id,
                stored.getFromCurrency(), stored.getToCurrency(), stored.getStatus());
        return stored;
    }

    public List<ConversionRecord> findAll() {
        return new ArrayList<>(records);
    }

    public List<ConversionRecord> findByCurrencyPair(String from, String to) {
        return currencyPairIndex.getOrDefault(pairKey(from, to), List.of());
    }

    public List<ConversionRecord> findByProvider(ProviderCodes code) {
        return records.stream()
                .filter(r -> code.equals(r.getProviderCode()))
                .collect(Collectors.toList());
    }

    public List<ConversionRecord> findByTimeRange(Instant from, Instant to) {
        return records.stream()
                .filter(r -> !r.getTimestamp().isBefore(from) && !r.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<ConversionRecord> findRecent(int limit) {
        return records.stream()
                .sorted(Comparator.comparing(ConversionRecord::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long count() {
        return records.size();
    }

    public long countByStatus(String status) {
        return records.stream().filter(r -> status.equals(r.getStatus())).count();
    }

    public Map<String, Long> getPopularPairs(int topN) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        r -> pairKey(r.getFromCurrency(), r.getToCurrency()),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public int removeOlderThan(Instant cutoff) {
        List<ConversionRecord> toRemove = records.stream()
                .filter(r -> r.getTimestamp().isBefore(cutoff))
                .collect(Collectors.toList());

        records.removeAll(toRemove);

        for (ConversionRecord removed : toRemove) {
            String key = pairKey(removed.getFromCurrency(), removed.getToCurrency());
            List<ConversionRecord> idx = currencyPairIndex.get(key);
            if (idx != null) {
                idx.remove(removed);
                if (idx.isEmpty()) {
                    currencyPairIndex.remove(key);
                }
            }
        }

        log.info("Cleaned up {} audit records older than {}", toRemove.size(), cutoff);
        return toRemove.size();
    }

    private static String pairKey(String from, String to) {
        return (from != null ? from : "") + "_" + (to != null ? to : "");
    }
}
