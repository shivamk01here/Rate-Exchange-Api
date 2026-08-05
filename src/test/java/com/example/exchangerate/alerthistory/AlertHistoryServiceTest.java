package com.example.exchangerate.alerthistory;

import com.example.exchangerate.alert.Alert;
import com.example.exchangerate.config.AlertHistoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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

    @Test
    void getStats_computesTotalsAndTopPairs() {
        config.setStatsTopPairsLimit(2);

        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("c@d.com").build();
        Alert alert3 = Alert.builder().id("a3")
                .fromCurrency("GBP").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("e@f.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("1.12"));
        alertHistoryService.recordTrigger(alert3, new BigDecimal("1.30"));

        AlertHistoryStats stats = alertHistoryService.getStats();

        assertEquals(3, stats.getTotalTriggers());
        assertEquals(3, stats.getUniqueAlerts());
        assertEquals(2, stats.getUniqueCurrencyPairs());
        assertEquals(2, stats.getTopPairs().size());
        assertEquals("USD/EUR", stats.getTopPairs().get(0).getKey());
        assertEquals(2L, stats.getTopPairs().get(0).getValue());
        assertEquals(3, stats.getEmailSentCount());
        assertEquals(0, stats.getWhatsappSentCount());
        assertNotNull(stats.getGeneratedAt());
    }

    @Test
    void getStats_whenEmpty_returnsZeros() {
        AlertHistoryStats stats = alertHistoryService.getStats();

        assertEquals(0, stats.getTotalTriggers());
        assertEquals(0, stats.getUniqueAlerts());
        assertEquals(0, stats.getUniqueCurrencyPairs());
        assertTrue(stats.getTopPairs().isEmpty());
    }

    @Test
    void getStats_countsTriggersInLast24Hours() {
        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("80")).email("a@b.com").build();
        alertHistoryService.recordTrigger(alert, new BigDecimal("82.00"));
        alertHistoryService.recordTrigger(alert, new BigDecimal("83.00"));

        AlertHistoryStats stats = alertHistoryService.getStats();

        assertEquals(2, stats.getTriggersLast24h());
        assertEquals(2, stats.getTriggersLast7d());
    }

    @Test
    void getRecentTriggers_filtersByWindow() {
        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        alertHistoryService.recordTrigger(alert, new BigDecimal("1.10"));

        AlertHistoryEntry oldEntry = AlertHistoryEntry.builder()
                .alertId("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com")
                .triggeredRate(new BigDecimal("1.00"))
                .triggeredAt(Instant.now().minus(47, ChronoUnit.HOURS))
                .build();
        alertHistoryRepository.save(oldEntry);

        List<AlertHistoryEntry> last24h = alertHistoryService.getRecentTriggers(24);

        assertEquals(1, last24h.size());
        assertEquals(alert.getId(), last24h.get(0).getAlertId());
    }

    @Test
    void getRecentTriggers_defaultsToConfigWindow() {
        config.setRecentWindowHours(48);

        Alert alert = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        alertHistoryService.recordTrigger(alert, new BigDecimal("1.10"));

        AlertHistoryEntry oldEntry = AlertHistoryEntry.builder()
                .alertId("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com")
                .triggeredRate(new BigDecimal("1.00"))
                .triggeredAt(Instant.now().minus(47, ChronoUnit.HOURS))
                .build();
        alertHistoryRepository.save(oldEntry);

        List<AlertHistoryEntry> result = alertHistoryService.getRecentTriggers(0);

        assertEquals(2, result.size());
    }

    @Test
    void getTopAlerts_ranksAlertsByTriggerCount() {
        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("c@d.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.15"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("82.00"));

        List<Map.Entry<String, Long>> top = alertHistoryService.getTopAlerts(0);

        assertEquals(2, top.size());
        assertEquals("a1", top.get(0).getKey());
        assertEquals(2L, top.get(0).getValue());
        assertEquals("a2", top.get(1).getKey());
        assertEquals(1L, top.get(1).getValue());
    }

    @Test
    void getTopAlerts_respectsLimit() {
        Alert alert1 = Alert.builder().id("a1")
                .fromCurrency("USD").toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("a@b.com").build();
        Alert alert2 = Alert.builder().id("a2")
                .fromCurrency("USD").toCurrency("INR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("c@d.com").build();
        Alert alert3 = Alert.builder().id("a3")
                .fromCurrency("GBP").toCurrency("USD")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(BigDecimal.ONE).email("e@f.com").build();

        alertHistoryService.recordTrigger(alert1, new BigDecimal("1.10"));
        alertHistoryService.recordTrigger(alert2, new BigDecimal("82.00"));
        alertHistoryService.recordTrigger(alert3, new BigDecimal("1.30"));

        List<Map.Entry<String, Long>> top = alertHistoryService.getTopAlerts(2);

        assertEquals(2, top.size());
        assertEquals("a1", top.get(0).getKey());
    }

    @Test
    void getTopAlerts_whenEmpty_returnsEmptyList() {
        List<Map.Entry<String, Long>> top = alertHistoryService.getTopAlerts(0);

        assertTrue(top.isEmpty());
    }
}
