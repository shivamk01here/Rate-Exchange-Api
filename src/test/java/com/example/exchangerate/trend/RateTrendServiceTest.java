package com.example.exchangerate.trend;

import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RateTrendServiceTest {

    private RateTrendService rateTrendService;
    private RateTrendRepository rateTrendRepository;
    private RateTrendMetricsCollector metricsCollector;

    @BeforeEach
    void setUp() {
        rateTrendRepository = new RateTrendRepository();
        RateTrendConfig config = new RateTrendConfig();
        config.setStabilityThresholdPercent(0.5);
        config.setDisplayLimit(10);
        metricsCollector = new RateTrendMetricsCollector();
        rateTrendService = new RateTrendService(rateTrendRepository, config, metricsCollector);
    }

    @Test
    void recordRate_savesSnapshot() {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .rate(new BigDecimal("83.50"))
                .status("SUCCESS")
                .build();

        RateSnapshot saved = rateTrendService.recordRate(response);

        assertNotNull(saved);
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertEquals(new BigDecimal("83.50"), saved.getRate());
        assertEquals("EXCHANGE_RATE_API", saved.getProviderCode());
    }

    @Test
    void recordRate_incrementsMetrics() {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .rate(new BigDecimal("83.50"))
                .status("SUCCESS")
                .build();

        rateTrendService.recordRate(response);

        assertEquals(1L, metricsCollector.getStats().get("recordCount"));
    }

    @Test
    void getSnapshots_returnsAllForPair() {
        recordMultipleRates("USD", "INR", new BigDecimal("83.00"), new BigDecimal("83.50"));

        List<RateSnapshot> snapshots = rateTrendService.getSnapshots("USD", "INR");

        assertEquals(2, snapshots.size());
    }

    @Test
    void getSnapshots_returnsEmptyForUnknownPair() {
        List<RateSnapshot> snapshots = rateTrendService.getSnapshots("XYZ", "ABC");

        assertTrue(snapshots.isEmpty());
    }

    @Test
    void getLatestSnapshot_returnsMostRecent() {
        recordMultipleRates("USD", "EUR", new BigDecimal("0.92"), new BigDecimal("0.93"));

        Optional<RateSnapshot> latest = rateTrendService.getLatestSnapshot("USD", "EUR");

        assertTrue(latest.isPresent());
        assertEquals(new BigDecimal("0.93"), latest.get().getRate());
    }

    @Test
    void getLatestSnapshot_returnsEmptyWhenNoneExist() {
        Optional<RateSnapshot> latest = rateTrendService.getLatestSnapshot("GBP", "JPY");

        assertFalse(latest.isPresent());
    }

    @Test
    void getRecentTrends_returnsCorrectCount() {
        recordMultipleRates("USD", "GBP", new BigDecimal("0.79"), new BigDecimal("0.80"), new BigDecimal("0.81"));

        List<RateTrend> trends = rateTrendService.getRecentTrends("USD", "GBP", 2);

        assertEquals(2, trends.size());
    }

    @Test
    void getRecentTrends_detectsRisingDirection() {
        recordMultipleRates("USD", "EUR", new BigDecimal("0.90"), new BigDecimal("0.95"));

        List<RateTrend> trends = rateTrendService.getRecentTrends("USD", "EUR", 5);

        assertFalse(trends.isEmpty());
        RateTrend latest = trends.get(0);
        assertEquals(RateTrend.TrendDirection.RISING, latest.getDirection());
        assertTrue(latest.getPercentChange().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getRecentTrends_detectsFallingDirection() {
        recordMultipleRates("USD", "EUR", new BigDecimal("0.95"), new BigDecimal("0.90"));

        List<RateTrend> trends = rateTrendService.getRecentTrends("USD", "EUR", 5);

        assertFalse(trends.isEmpty());
        RateTrend latest = trends.get(0);
        assertEquals(RateTrend.TrendDirection.FALLING, latest.getDirection());
        assertTrue(latest.getPercentChange().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void getRecentTrends_detectsStableDirection() {
        recordMultipleRates("USD", "EUR", new BigDecimal("0.9200"), new BigDecimal("0.9201"));

        List<RateTrend> trends = rateTrendService.getRecentTrends("USD", "EUR", 5);

        assertFalse(trends.isEmpty());
        assertEquals(RateTrend.TrendDirection.STABLE, trends.get(0).getDirection());
    }

    @Test
    void getTrendSummary_returnsEmptySummaryWhenNoData() {
        TrendSummary summary = rateTrendService.getTrendSummary("USD", "INR");

        assertEquals(0, summary.getTotalSnapshots());
        assertEquals(RateTrend.TrendDirection.STABLE, summary.getOverallDirection());
        assertEquals(BigDecimal.ZERO, summary.getOverallPercentChange());
    }

    @Test
    void getTrendSummary_computesCorrectStats() {
        recordMultipleRates("USD", "INR", new BigDecimal("83.00"), new BigDecimal("83.50"), new BigDecimal("84.00"));

        TrendSummary summary = rateTrendService.getTrendSummary("USD", "INR");

        assertEquals(3, summary.getTotalSnapshots());
        assertEquals(new BigDecimal("84.00"), summary.getLatestRate());
        assertEquals(new BigDecimal("83.00"), summary.getOldestRate());
        assertEquals(new BigDecimal("84.00"), summary.getHighestRate());
        assertEquals(new BigDecimal("83.00"), summary.getLowestRate());
        assertNotNull(summary.getAverageRate());
        assertEquals(RateTrend.TrendDirection.RISING, summary.getOverallDirection());
    }

    @Test
    void getStats_returnsCorrectCounts() {
        rateTrendService.recordRate(buildResponse("USD", "INR", "83.50"));
        rateTrendService.recordRate(buildResponse("EUR", "GBP", "0.80"));

        RateTrendService.MapStats stats = rateTrendService.getStats();

        assertEquals(2, stats.totalSnapshots());
        assertEquals(2, stats.pairCounts().size());
    }

    @Test
    void clearAll_removesAllData() {
        rateTrendService.recordRate(buildResponse("USD", "INR", "83.50"));
        rateTrendService.recordRate(buildResponse("EUR", "GBP", "0.80"));

        rateTrendService.clearAll();

        assertEquals(0, rateTrendService.getStats().totalSnapshots());
    }

    @Test
    void clearByPair_removesOnlyPairData() {
        rateTrendService.recordRate(buildResponse("USD", "INR", "83.50"));
        rateTrendService.recordRate(buildResponse("EUR", "GBP", "0.80"));

        rateTrendService.clearByPair("USD", "INR");

        assertEquals(1, rateTrendService.getStats().totalSnapshots());
        assertTrue(rateTrendService.getSnapshots("EUR", "GBP").size() > 0);
    }

    private void recordMultipleRates(String from, String to, BigDecimal... rates) {
        for (BigDecimal rate : rates) {
            rateTrendService.recordRate(buildResponse(from, to, rate.toPlainString()));
        }
    }

    private ExchangeRateResponse buildResponse(String from, String to, String rate) {
        return ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency(from)
                .toCurrency(to)
                .rate(new BigDecimal(rate))
                .status("SUCCESS")
                .build();
    }
}
