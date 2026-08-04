package com.example.exchangerate.controllers;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.alerthistory.AlertHistoryEntry;
import com.example.exchangerate.alerthistory.AlertHistoryRepository;
import com.example.exchangerate.alerthistory.AlertHistoryService;
import com.example.exchangerate.alerthistory.AlertHistoryStats;
import com.example.exchangerate.config.AlertHistoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlertHistoryControllerTest {

    private AlertHistoryController controller;
    private AlertHistoryService alertHistoryService;

    @BeforeEach
    void setUp() {
        AlertHistoryConfig config = new AlertHistoryConfig();
        AlertHistoryRepository repository = new AlertHistoryRepository();
        alertHistoryService = new AlertHistoryService(repository, config);
        controller = new AlertHistoryController(alertHistoryService);
    }

    private AlertHistoryEntry createTrigger(String alertId, String from, String to, BigDecimal rate) {
        Alert alert = Alert.builder()
                .id(alertId)
                .fromCurrency(from)
                .toCurrency(to)
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE)
                .email("test@example.com")
                .build();
        return alertHistoryService.recordTrigger(alert, rate);
    }

    @Test
    void getAllEntries_returnsPaginatedResults() {
        for (int i = 0; i < 5; i++) {
            createTrigger("a1", "USD", "EUR", BigDecimal.valueOf(1.0 + i));
        }

        List<AlertHistoryEntry> page0 = controller.getAllEntries(0, 3);

        assertEquals(3, page0.size());
    }

    @Test
    void getEntry_returnsById() {
        AlertHistoryEntry created = createTrigger("a1", "USD", "INR", new BigDecimal("83.45"));

        AlertHistoryEntry result = controller.getEntry(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("INR", result.getToCurrency());
    }

    @Test
    void getEntry_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getEntry("bad-id"));
    }

    @Test
    void getEntriesByAlertId_filtersCorrectly() {
        createTrigger("a1", "USD", "EUR", BigDecimal.ONE);
        createTrigger("a1", "USD", "INR", BigDecimal.TEN);
        createTrigger("a2", "EUR", "USD", BigDecimal.ONE);

        List<AlertHistoryEntry> result = controller.getEntriesByAlertId("a1");

        assertEquals(2, result.size());
    }

    @Test
    void getEntriesByPair_filtersCorrectly() {
        createTrigger("a1", "USD", "INR", BigDecimal.TEN);
        createTrigger("a1", "USD", "EUR", BigDecimal.TEN);

        List<AlertHistoryEntry> result = controller.getEntriesByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getCount_returnsTotalCount() {
        createTrigger("a1", "USD", "EUR", BigDecimal.ONE);

        Map<String, Object> result = controller.getCount(null);

        assertEquals(1L, result.get("count"));
    }

    @Test
    void getCount_returnsAlertIdCount() {
        createTrigger("a1", "USD", "INR", BigDecimal.TEN);
        createTrigger("a1", "USD", "EUR", BigDecimal.ONE);

        Map<String, Object> result = controller.getCount("a1");

        assertEquals(2L, result.get("count"));
    }

    @Test
    void deleteEntry_returnsDeleted() {
        AlertHistoryEntry created = createTrigger("a1", "USD", "CAD", BigDecimal.ONE);

        Map<String, String> result = controller.deleteEntry(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void clearHistory_returnsCleared() {
        createTrigger("a1", "USD", "EUR", BigDecimal.ONE);

        Map<String, String> result = controller.clearHistory();

        assertEquals("cleared", result.get("status"));
        assertEquals(0, controller.getAllEntries(0, 100).size());
    }

    @Test
    void getStats_returnsAggregatedStats() {
        createTrigger("a1", "USD", "EUR", BigDecimal.ONE);
        createTrigger("a1", "USD", "INR", BigDecimal.TEN);
        createTrigger("a2", "USD", "INR", BigDecimal.TEN);

        AlertHistoryStats stats = controller.getStats();

        assertEquals(3, stats.getTotalTriggers());
        assertEquals(2, stats.getUniqueAlerts());
        assertEquals(2, stats.getUniqueCurrencyPairs());
        assertEquals("USD/INR", stats.getTopPairs().get(0).getKey());
        assertEquals(2L, stats.getTopPairs().get(0).getValue());
    }

    @Test
    void getStats_whenEmpty_returnsEmptyStats() {
        AlertHistoryStats stats = controller.getStats();

        assertEquals(0, stats.getTotalTriggers());
        assertTrue(stats.getTopPairs().isEmpty());
    }

    @Test
    void getRecentTriggers_returnsEntriesWithinWindow() {
        createTrigger("a1", "USD", "EUR", BigDecimal.ONE);
        createTrigger("a1", "USD", "INR", BigDecimal.TEN);

        List<AlertHistoryEntry> recent = controller.getRecentTriggers(24);

        assertEquals(2, recent.size());
    }
}
