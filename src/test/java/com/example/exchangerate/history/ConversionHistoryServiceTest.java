package com.example.exchangerate.history;

import com.example.exchangerate.config.ConversionHistoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversionHistoryServiceTest {

    private ConversionHistoryService historyService;
    private ConversionHistoryRepository historyRepository;
    private ConversionHistoryConfig config;

    @BeforeEach
    void setUp() {
        config = new ConversionHistoryConfig();
        config.setMaxPageSize(50);
        historyRepository = new ConversionHistoryRepository();
        historyService = new ConversionHistoryService(historyRepository, config);
    }

    @Test
    void recordConversion_savesAndReturnsEntry() {
        ConversionHistoryEntry entry = ConversionHistoryEntry.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("0.85"))
                .convertedAmount(new BigDecimal("85"))
                .provider("EXCHANGE_RATE_API")
                .status("SUCCESS")
                .build();

        ConversionHistoryEntry saved = historyService.recordConversion(entry);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("EUR", saved.getToCurrency());
        assertEquals(new BigDecimal("100"), saved.getAmount());
        assertEquals(new BigDecimal("0.85"), saved.getRate());
    }

    @Test
    void getEntry_returnsEntryWhenExists() {
        ConversionHistoryEntry saved = historyService.recordConversion(
                ConversionHistoryEntry.builder()
                        .fromCurrency("USD").toCurrency("INR")
                        .amount(new BigDecimal("100"))
                        .rate(new BigDecimal("83.45"))
                        .convertedAmount(new BigDecimal("8345"))
                        .status("SUCCESS").build());

        ConversionHistoryEntry found = historyService.getEntry(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("USD", found.getFromCurrency());
        assertEquals("INR", found.getToCurrency());
    }

    @Test
    void getEntry_returnsEmptyWhenNotFound() {
        assertTrue(historyService.getEntry("nonexistent").isEmpty());
    }

    @Test
    void getAllEntries_returnsAllEntries() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("GBP").toCurrency("USD").amount(new BigDecimal("50")).build());

        List<ConversionHistoryEntry> all = historyService.getAllEntries();

        assertEquals(2, all.size());
    }

    @Test
    void getEntries_withPagination_returnsCorrectPage() {
        for (int i = 0; i < 10; i++) {
            historyService.recordConversion(ConversionHistoryEntry.builder()
                    .fromCurrency("USD").toCurrency("EUR")
                    .amount(BigDecimal.valueOf(i + 1)).build());
        }

        List<ConversionHistoryEntry> page0 = historyService.getEntries(0, 5);
        List<ConversionHistoryEntry> page1 = historyService.getEntries(1, 5);

        assertEquals(5, page0.size());
        assertEquals(5, page1.size());
    }

    @Test
    void getEntries_respectsMaxPageSize() {
        config.setMaxPageSize(10);

        List<ConversionHistoryEntry> result = historyService.getEntries(0, 100);

        assertTrue(result.size() <= 10);
    }

    @Test
    void getEntriesByCurrencyPair_filtersCorrectly() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.TEN).build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(new BigDecimal("20")).build());

        List<ConversionHistoryEntry> result = historyService.getEntriesByCurrencyPair("USD", "INR");

        assertEquals(2, result.size());
    }

    @Test
    void getEntriesByStatus_filtersCorrectly() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").status("SUCCESS").build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").status("FAILED").build());

        List<ConversionHistoryEntry> success = historyService.getEntriesByStatus("SUCCESS");

        assertEquals(1, success.size());
        assertEquals("SUCCESS", success.get(0).getStatus());
    }

    @Test
    void deleteEntry_removesEntry() {
        ConversionHistoryEntry saved = historyService.recordConversion(
                ConversionHistoryEntry.builder()
                        .fromCurrency("USD").toCurrency("JPY").amount(BigDecimal.ONE).build());

        historyService.deleteEntry(saved.getId());

        assertTrue(historyService.getEntry(saved.getId()).isEmpty());
    }

    @Test
    void clearHistory_removesAllEntries() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("GBP").toCurrency("USD").amount(BigDecimal.ONE).build());

        historyService.clearHistory();

        assertEquals(0, historyService.getTotalCount());
    }

    @Test
    void getStatistics_returnsCorrectStats() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("0.85"))
                .status("SUCCESS").build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .amount(new BigDecimal("200"))
                .rate(new BigDecimal("0.73"))
                .status("SUCCESS").build());

        Map<String, Object> stats = historyService.getStatistics();

        assertEquals(2L, stats.get("totalConversions"));
        assertNotNull(stats.get("averageRate"));
        assertNotNull(stats.get("totalVolume"));
        assertNotNull(stats.get("successRate"));
        assertNotNull(stats.get("conversionsByPair"));
    }

    @Test
    void getStatistics_returnsDefaultsForEmptyHistory() {
        Map<String, Object> stats = historyService.getStatistics();

        assertEquals(0L, stats.get("totalConversions"));
        assertEquals(BigDecimal.ZERO, stats.get("averageRate"));
        assertEquals(BigDecimal.ZERO, stats.get("totalVolume"));
    }

    @Test
    void getRecentActivity_returnsActivityForPeriod() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .status("SUCCESS").build());

        Map<String, Object> activity = historyService.getRecentActivity(24);

        assertEquals(24, activity.get("periodHours"));
        assertEquals(1L, activity.get("totalConversions"));
        assertEquals(1L, activity.get("successCount"));
        assertEquals(0L, activity.get("failureCount"));
    }

    @Test
    void getTotalCount_returnsCorrectCount() {
        assertEquals(0, historyService.getTotalCount());

        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());

        assertEquals(1, historyService.getTotalCount());
    }

    @Test
    void getCountByCurrencyPair_returnsCorrectCount() {
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(new BigDecimal("20")).build());
        historyService.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.TEN).build());

        long count = historyService.getCountByCurrencyPair("USD", "INR");

        assertEquals(2, count);
    }
}
