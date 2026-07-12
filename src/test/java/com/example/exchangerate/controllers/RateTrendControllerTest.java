package com.example.exchangerate.controllers;

import com.example.exchangerate.trend.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RateTrendControllerTest {

    private RateTrendController controller;
    private RateTrendService service;

    @BeforeEach
    void setUp() {
        RateTrendRepository repository = new RateTrendRepository();
        RateTrendConfig config = new RateTrendConfig();
        config.setStabilityThresholdPercent(0.5);
        config.setDisplayLimit(10);
        RateTrendMetricsCollector metrics = new RateTrendMetricsCollector();
        service = new RateTrendService(repository, config, metrics);
        controller = new RateTrendController(service);
    }

    @Test
    void getSnapshots_returnsSnapshotsForPair() {
        service.recordRate(buildResponse("USD", "INR", "83.50"));
        service.recordRate(buildResponse("USD", "INR", "83.60"));

        List<RateSnapshot> snapshots = controller.getSnapshots("USD", "INR");

        assertEquals(2, snapshots.size());
    }

    @Test
    void getSnapshots_returnsEmptyForUnknownPair() {
        List<RateSnapshot> snapshots = controller.getSnapshots("XYZ", "ABC");

        assertTrue(snapshots.isEmpty());
    }

    @Test
    void getLatestSnapshot_returnsLatestSnapshot() {
        service.recordRate(buildResponse("USD", "EUR", "0.92"));
        service.recordRate(buildResponse("USD", "EUR", "0.93"));

        RateSnapshot latest = controller.getLatestSnapshot("USD", "EUR");

        assertEquals(new BigDecimal("0.93"), latest.getRate());
    }

    @Test
    void getLatestSnapshot_throwsWhenNotFound() {
        assertThrows(ResponseStatusException.class,
                () -> controller.getLatestSnapshot("GBP", "JPY"));
    }

    @Test
    void getRecentTrends_returnsTrends() {
        service.recordRate(buildResponse("USD", "GBP", "0.79"));
        service.recordRate(buildResponse("USD", "GBP", "0.80"));

        List<RateTrend> trends = controller.getRecentTrends("USD", "GBP", 5);

        assertFalse(trends.isEmpty());
    }

    @Test
    void getTrendSummary_returnsSummary() {
        service.recordRate(buildResponse("USD", "INR", "83.00"));
        service.recordRate(buildResponse("USD", "INR", "83.50"));

        TrendSummary summary = controller.getTrendSummary("USD", "INR");

        assertEquals(2, summary.getTotalSnapshots());
        assertEquals("USD", summary.getFromCurrency());
        assertEquals("INR", summary.getToCurrency());
    }

    @Test
    void getStats_returnsStatsMap() {
        service.recordRate(buildResponse("USD", "INR", "83.50"));

        Map<String, Object> stats = controller.getStats();

        assertEquals(1L, stats.get("totalSnapshots"));
        assertNotNull(stats.get("pairCounts"));
    }

    @Test
    void clearAll_clearsAllData() {
        service.recordRate(buildResponse("USD", "INR", "83.50"));

        Map<String, String> result = controller.clearAll();

        assertEquals("cleared", result.get("status"));
        assertEquals(0, service.getStats().totalSnapshots());
    }

    @Test
    void clearByPair_clearsOnlyPairData() {
        service.recordRate(buildResponse("USD", "INR", "83.50"));
        service.recordRate(buildResponse("EUR", "GBP", "0.80"));

        Map<String, String> result = controller.clearByPair("USD", "INR");

        assertEquals("cleared", result.get("status"));
        assertEquals("USD", result.get("from"));
        assertEquals("INR", result.get("to"));
        assertEquals(1, service.getStats().totalSnapshots());
    }

    private ExchangeRateResponse buildResponse(String from, String to, String rate) {
        return com.example.exchangerate.models.ExchangeRateResponse.builder()
                .providerCode(com.example.exchangerate.models.ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency(from)
                .toCurrency(to)
                .rate(new BigDecimal(rate))
                .status("SUCCESS")
                .build();
    }
}
