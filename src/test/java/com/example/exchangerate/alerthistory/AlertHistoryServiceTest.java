package com.example.exchangerate.alerthistory;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.config.AlertHistoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertHistoryServiceTest {

    private AlertHistoryService alertHistoryService;
    private AlertHistoryRepository alertHistoryRepository;
    private AlertHistoryConfig config;

    @BeforeEach
    void setUp() {
        config = new AlertHistoryConfig();
        config.setMaxPageSize(50);
        config.setMaxEntries(100);
        alertHistoryRepository = new AlertHistoryRepository();
        alertHistoryService = new AlertHistoryService(alertHistoryRepository, config);
    }

    @Test
    void recordTrigger_savesAndReturnsEntry() {
        Alert alert = Alert.builder()
                .id("alert-1")
                .fromCurrency("USD")
                .toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("85.00"))
                .email("test@example.com")
                .build();

        AlertHistoryEntry saved = alertHistoryService.recordTrigger(alert, new BigDecimal("86.50"));

        assertNotNull(saved.getId());
        assertEquals("alert-1", saved.getAlertId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertEquals(new BigDecimal("86.50"), saved.getTriggeredRate());
        assertTrue(saved.isEmailSent());
    }

    @Test
    void getEntry_returnsEntryWhenExists() {
        Alert alert = Alert.builder().id("alert-1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("1.10")).email("a@b.com").build();
        AlertHistoryEntry saved = alertHistoryService.recordTrigger(alert, new BigDecimal("1.15"));

        AlertHistoryEntry found = alertHistoryService.getEntry(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("alert-1", found.getAlertId());
    }

    @Test
    void getEntry_returnsEmptyWhenNotFound() {
        assertTrue(alertHistoryService.getEntry("nonexistent").isEmpty());
    }

    @Test
    void getAllEntries_returnsAllEntries() {
        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("GBP").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_BELOW)
                .threshold(BigDecimal.TEN).email("c@d.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.20"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("1.25"));

        List<AlertHistoryEntry> all = alertHistoryService.getAllEntries();

        assertEquals(2, all.size());
    }

    @Test
    void getEntries_withPagination_returnsCorrectPage() {
        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();

        for (int i = 0; i < 10; i++) {
            alertHistoryService.recordTrigger(alert, BigDecimal.valueOf(1.0 + i));
        }

        List<AlertHistoryEntry> page0 = alertHistoryService.getEntries(0, 5);
        List<AlertHistoryEntry> page1 = alertHistoryService.getEntries(1, 5);

        assertEquals(5, page0.size());
        assertEquals(5, page1.size());
    }

    @Test
    void getEntries_respectsMaxPageSize() {
        config.setMaxPageSize(10);

        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();

        for (int i = 0; i < 15; i++) {
            alertHistoryService.recordTrigger(alert, BigDecimal.valueOf(1.0 + i));
        }

        List<AlertHistoryEntry> result = alertHistoryService.getEntries(0, 100);

        assertTrue(result.size() <= 10);
    }

    @Test
    void getEntriesByAlertId_filtersCorrectly() {
        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("80")).email("b@c.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("82.00"));
        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.15"));

        List<AlertHistoryEntry> forAlert1 = alertHistoryService.getEntriesByAlertId("a1");

        assertEquals(2, forAlert1.size());
    }

    @Test
    void getEntriesByCurrencyPair_filtersCorrectly() {
        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("80")).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("c@d.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("82.00"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert1, new BigDecimal("83.00"));

        List<AlertHistoryEntry> result = alertHistoryService.getEntriesByCurrencyPair("USD", "INR");

        assertEquals(2, result.size());
    }

    @Test
    void getTotalCount_returnsCorrectCount() {
        assertEquals(0, alertHistoryService.getTotalCount());

        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        alertHistoryService.recordTrigger(alert, new BigDecimal("1.10"));

        assertEquals(1, alertHistoryService.getTotalCount());
    }

    @Test
    void getCountByAlertId_returnsCorrectCount() {
        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("80")).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("c@d.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("82.00"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert1, new BigDecimal("83.00"));

        assertEquals(2, alertHistoryService.getCountByAlertId("a1"));
        assertEquals(1, alertHistoryService.getCountByAlertId("a2"));
    }

    @Test
    void deleteEntry_removesEntry() {
        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("JPY")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("150")).email("x@y.com").build();
        AlertHistoryEntry saved = alertHistoryService.recordTrigger(alert, new BigDecimal("152.00"));

        alertHistoryService.deleteEntry(saved.getId());

        assertTrue(alertHistoryService.getEntry(saved.getId()).isEmpty());
    }

    @Test
    void clearHistory_removesAllEntries() {
        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        alertHistoryService.recordTrigger(alert, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert, new BigDecimal("1.15"));

        alertHistoryService.clearHistory();

        assertEquals(0, alertHistoryService.getTotalCount());
    }

    @Test
    void recordTrigger_trimsToMaxEntries() {
        config.setMaxEntries(5);

        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();

        for (int i = 0; i < 10; i++) {
            alertHistoryService.recordTrigger(alert, BigDecimal.valueOf(1.0 + i));
        }

        assertTrue(alertHistoryService.getTotalCount() <= 5);
    }
}
