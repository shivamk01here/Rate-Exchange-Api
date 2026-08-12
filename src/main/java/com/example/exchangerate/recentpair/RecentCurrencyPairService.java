package com.example.exchangerate.recentpair;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentCurrencyPairService {

    private final RecentCurrencyPairRepository pairRepository;

    public RecentCurrencyPair recordPair(String from, String to) {
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new IllegalArgumentException("fromCurrency and toCurrency are required");
        }
        RecentCurrencyPair saved = pairRepository.recordUse(from, to, Instant.now());
        log.info("Recent pair recorded: {}->{}", from, to);
        return saved;
    }

    public Optional<RecentCurrencyPair> getPair(String from, String to) {
        return pairRepository.findByPair(from, to);
    }

    public List<RecentCurrencyPair> getRecentPairs() {
        return pairRepository.findAllRecent();
    }

    public List<RecentCurrencyPair> getTopRecent(int limit) {
        return pairRepository.findTopRecent(limit);
    }

    public List<RecentCurrencyPair> getMostUsed(int limit) {
        return pairRepository.findMostUsed(limit);
    }

    public boolean deletePair(String from, String to) {
        boolean deleted = pairRepository.deleteByPair(from, to);
        if (deleted) {
            log.info("Recent pair deleted: {}->{}", from, to);
        }
        return deleted;
    }

    public void clearAll() {
        pairRepository.clear();
    }

    public long getPairCount() {
        return pairRepository.count();
    }
}
