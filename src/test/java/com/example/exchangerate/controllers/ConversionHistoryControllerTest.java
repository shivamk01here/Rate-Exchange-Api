package com.example.exchangerate.controllers;

import com.example.exchangerate.config.ConversionHistoryConfig;
import com.example.exchangerate.history.ConversionHistoryEntry;
import com.example.exchangerate.history.ConversionHistoryRepository;
import com.example.exchangerate.history.ConversionHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversionHistoryControllerTest {

    private ConversionHistoryController controller;

    @BeforeEach
    void setUp() {
        ConversionHistoryConfig config = new ConversionHistoryConfig();
        ConversionHistoryRepository repository = new ConversionHistoryRepository();
        ConversionHistoryService service = new ConversionHistoryService(repository, config);
        controller = new ConversionHistoryController(service);
    }

    @Test
    void recordConversion_returnsSavedEntry() {
        ConversionHistoryEntry entry = ConversionHistoryEntry.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("0.85"))
                .convertedAmount(new BigDecimal("85"))
                .status("SUCCESS")
                .build();

        ConversionHistoryEntry result = controller.recordConversion(entry);

        assertNotNull(result.getId());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
    }

    @Test
    void getAllEntries_returnsPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            controller.recordConversion(ConversionHistoryEntry.builder()
                    .fromCurrency("USD").toCurrency("EUR")
                    .amount(BigDecimal.valueOf(i + 1)).build());
        }

        List<ConversionHistoryEntry> page0 = controller.getAllEntries(0, 3);

        assertEquals(3, page0.size());
    }

    @Test
    void getEntry_returnsById() {
        ConversionHistoryEntry created = controller.recordConversion(
                ConversionHistoryEntry.builder()
                        .fromCurrency("USD").toCurrency("INR")
                        .amount(new BigDecimal("100"))
                        .rate(new BigDecimal("83.45"))
                        .convertedAmount(new BigDecimal("8345"))
                        .status("SUCCESS").build());

        ConversionHistoryEntry result = controller.getEntry(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("INR", result.getToCurrency());
    }

    @Test
    void getEntry_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getEntry("bad-id"));
    }

    @Test
    void getEntriesByPair_filtersCorrectly() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.TEN).build());

        List<ConversionHistoryEntry> result = controller.getEntriesByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getEntriesByStatus_filtersCorrectly() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").status("SUCCESS").build());
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").status("FAILED").build());

        List<ConversionHistoryEntry> result = controller.getEntriesByStatus("SUCCESS");

        assertEquals(1, result.size());
        assertEquals("SUCCESS", result.get(0).getStatus());
    }

    @Test
    void getCount_returnsTotalCount() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());

        Map<String, Object> result = controller.getCount(null, null);

        assertEquals(1L, result.get("count"));
    }

    @Test
    void getCount_returnsPairCount() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("INR").amount(new BigDecimal("20")).build());

        Map<String, Object> result = controller.getCount("USD", "INR");

        assertEquals(2L, result.get("count"));
    }

    @Test
    void getStatistics_returnsStats() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("0.85"))
                .status("SUCCESS").build());

        Map<String, Object> stats = controller.getStatistics();

        assertNotNull(stats.get("totalConversions"));
        assertNotNull(stats.get("averageRate"));
    }

    @Test
    void getRecentActivity_returnsActivity() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .status("SUCCESS").build());

        Map<String, Object> activity = controller.getRecentActivity(24);

        assertEquals(24, activity.get("periodHours"));
        assertEquals(1L, activity.get("totalConversions"));
    }

    @Test
    void deleteEntry_returnsDeleted() {
        ConversionHistoryEntry created = controller.recordConversion(
                ConversionHistoryEntry.builder()
                        .fromCurrency("USD").toCurrency("CAD")
                        .amount(BigDecimal.ONE).build());

        Map<String, String> result = controller.deleteEntry(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void clearHistory_returnsCleared() {
        controller.recordConversion(ConversionHistoryEntry.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());

        Map<String, String> result = controller.clearHistory();

        assertEquals("cleared", result.get("status"));
        assertEquals(0, controller.getAllEntries(0, 100).size());
    }
}
