package com.example.exchangerate.trending;

import com.example.exchangerate.history.ConversionHistoryEntry;
import com.example.exchangerate.history.ConversionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrendingPairServiceTest {

    private ConversionHistoryRepository historyRepository;
    private TrendingPairService service;

    @BeforeEach
    void setUp() {
        historyRepository = new ConversionHistoryRepository();
        service = new TrendingPairService(historyRepository);
    }

    private void record(String from, String to, BigDecimal amount, Instant timestamp) {
        historyRepository.save(ConversionHistoryEntry.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .amount(amount)
                .rate(new BigDecimal("1.0"))
                .convertedAmount(amount)
                .status("SUCCESS")
                .timestamp(timestamp)
                .build());
    }

    @Test
    void getTrendingPairs_ordersByVolume() {
        record("USD", "INR", new BigDecimal("500"), Instant.parse("2026-08-01T10:00:00Z"));
        record("USD", "INR", new BigDecimal("400"), Instant.parse("2026-08-02T10:00:00Z"));
        record("EUR", "GBP", new BigDecimal("100"), Instant.parse("2026-08-03T10:00:00Z"));

        List<TrendingPair> trending = service.getTrendingPairs(10);

        assertEquals(2, trending.size());
        assertEquals("USD", trending.get(0).getFromCurrency());
        assertEquals(new BigDecimal("900"), trending.get(0).getTotalVolume());
        assertEquals(2, trending.get(0).getConversionCount());
    }

    @Test
    void getTrendingPairs_limitsResults() {
        record("USD", "INR", new BigDecimal("100"), Instant.parse("2026-08-01T10:00:00Z"));
        record("EUR", "GBP", new BigDecimal("200"), Instant.parse("2026-08-01T10:00:00Z"));

        List<TrendingPair> trending = service.getTrendingPairs(1);

        assertEquals(1, trending.size());
        assertEquals("EUR", trending.get(0).getFromCurrency());
    }

    @Test
    void getTrendingPairsByCount_ordersByCount() {
        record("USD", "INR", new BigDecimal("50"), Instant.parse("2026-08-01T10:00:00Z"));
        record("USD", "INR", new BigDecimal("60"), Instant.parse("2026-08-02T10:00:00Z"));
        record("EUR", "GBP", new BigDecimal("500"), Instant.parse("2026-08-03T10:00:00Z"));

        List<TrendingPair> byCount = service.getTrendingPairsByCount(10);

        assertEquals("USD", byCount.get(0).getFromCurrency());
        assertEquals(2, byCount.get(0).getConversionCount());
    }

    @Test
    void getTrendingPairsSince_filtersByWindow() {
        Instant now = Instant.parse("2026-08-05T10:00:00Z");
        record("USD", "INR", new BigDecimal("100"), now.minusSeconds(3600));
        record("EUR", "GBP", new BigDecimal("200"), now.minusSeconds(60 * 60 * 48));

        List<TrendingPair> recent = service.getTrendingPairsSince(24, 10);

        assertEquals(1, recent.size());
        assertEquals("USD", recent.get(0).getFromCurrency());
    }

    @Test
    void getDistinctPairCount_countsUniquePairs() {
        record("USD", "INR", new BigDecimal("100"), Instant.parse("2026-08-01T10:00:00Z"));
        record("USD", "INR", new BigDecimal("50"), Instant.parse("2026-08-02T10:00:00Z"));
        record("EUR", "GBP", new BigDecimal("200"), Instant.parse("2026-08-01T10:00:00Z"));

        assertEquals(2, service.getDistinctPairCount());
    }

    @Test
    void getTrendingPairs_latestTimestampIsNewest() {
        record("USD", "INR", new BigDecimal("100"), Instant.parse("2026-08-01T10:00:00Z"));
        record("USD", "INR", new BigDecimal("50"), Instant.parse("2026-08-03T10:00:00Z"));

        List<TrendingPair> trending = service.getTrendingPairs(10);

        assertEquals(Instant.parse("2026-08-03T10:00:00Z"), trending.get(0).getLatestTimestamp());
    }

    @Test
    void getTrendingPairs_emptyHistoryReturnsEmpty() {
        assertTrue(service.getTrendingPairs(10).isEmpty());
        assertEquals(0, service.getDistinctPairCount());
    }
}
