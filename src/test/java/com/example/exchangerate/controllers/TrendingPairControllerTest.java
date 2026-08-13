package com.example.exchangerate.controllers;

import com.example.exchangerate.history.ConversionHistoryEntry;
import com.example.exchangerate.history.ConversionHistoryRepository;
import com.example.exchangerate.trending.TrendingPair;
import com.example.exchangerate.trending.TrendingPairService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TrendingPairControllerTest {

    private TrendingPairController controller;

    @BeforeEach
    void setUp() {
        ConversionHistoryRepository historyRepository = new ConversionHistoryRepository();
        TrendingPairService service = new TrendingPairService(historyRepository);
        controller = new TrendingPairController(service);

        historyRepository.save(ConversionHistoryEntry.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("500"))
                .rate(new BigDecimal("1.0"))
                .convertedAmount(new BigDecimal("500"))
                .status("SUCCESS")
                .timestamp(Instant.now())
                .build());
        historyRepository.save(ConversionHistoryEntry.builder()
                .fromCurrency("EUR")
                .toCurrency("GBP")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("1.0"))
                .convertedAmount(new BigDecimal("100"))
                .status("SUCCESS")
                .timestamp(Instant.now())
                .build());
    }

    @Test
    void getTrendingPairs_returnsVolumeOrdered() {
        List<TrendingPair> trending = controller.getTrendingPairs(10);

        assertEquals(2, trending.size());
        assertEquals("USD", trending.get(0).getFromCurrency());
    }

    @Test
    void getTrendingPairs_honorsLimit() {
        List<TrendingPair> trending = controller.getTrendingPairs(1);

        assertEquals(1, trending.size());
    }

    @Test
    void getTrendingPairsByCount_returnsCounts() {
        List<TrendingPair> byCount = controller.getTrendingPairsByCount(10);

        assertEquals(2, byCount.size());
        assertEquals(1, byCount.get(0).getConversionCount());
    }

    @Test
    void getTrendingPairsSince_returnsWithinWindow() {
        List<TrendingPair> recent = controller.getTrendingPairsSince(24, 10);

        assertEquals(2, recent.size());
    }

    @Test
    void getDistinctPairCount_returnsCount() {
        Map<String, Object> result = controller.getDistinctPairCount();

        assertEquals(2L, result.get("count"));
    }
}
