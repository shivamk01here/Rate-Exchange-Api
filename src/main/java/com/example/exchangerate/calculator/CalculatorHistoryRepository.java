package com.example.exchangerate.calculator;

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
public class CalculatorHistoryRepository {

    private final ConcurrentHashMap<String, CalculatorHistory> historyMap = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<CalculatorHistory> historyList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public CalculatorHistory save(CalculatorHistory history) {
        String id = history.getId() != null ? history.getId() : String.valueOf(idCounter.incrementAndGet());
        CalculatorHistory stored = CalculatorHistory.builder()
                .id(id)
                .fromCurrency(history.getFromCurrency())
                .toCurrency(history.getToCurrency())
                .amount(history.getAmount())
                .rate(history.getRate())
                .convertedAmount(history.getConvertedAmount())
                .provider(history.getProvider())
                .favorite(history.isFavorite())
                .calculatedAt(history.getCalculatedAt() != null ? history.getCalculatedAt() : java.time.Instant.now())
                .build();

        if (historyMap.putIfAbsent(id, stored) == null) {
            historyList.add(stored);
        } else {
            historyMap.put(id, stored);
            for (int i = 0; i < historyList.size(); i++) {
                if (id.equals(historyList.get(i).getId())) {
                    historyList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("CalculatorHistory saved: id={} {}->{} amount={}",
                id, stored.getFromCurrency(), stored.getToCurrency(), stored.getAmount());
        return stored;
    }

    public Optional<CalculatorHistory> findById(String id) {
        return Optional.ofNullable(historyMap.get(id));
    }

    public List<CalculatorHistory> findAll() {
        return new ArrayList<>(historyList);
    }

    public List<CalculatorHistory> findByCurrencyPair(String from, String to) {
        return historyList.stream()
                .filter(h -> from.equalsIgnoreCase(h.getFromCurrency())
                        && to.equalsIgnoreCase(h.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<CalculatorHistory> findFavorites() {
        return historyList.stream()
                .filter(CalculatorHistory::isFavorite)
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        CalculatorHistory removed = historyMap.remove(id);
        if (removed != null) {
            historyList.remove(removed);
            log.info("CalculatorHistory deleted: id={}", id);
            return true;
        }
        return false;
    }

    public void deleteAll() {
        historyMap.clear();
        historyList.clear();
        log.info("All calculator history cleared");
    }

    public long count() {
        return historyMap.size();
    }
}
