package com.example.exchangerate.calculator;

import com.example.exchangerate.config.CalculatorConfig;
import com.example.exchangerate.controllers.CalculatorSummaryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorSummaryControllerTest {

    private CalculatorSummaryController controller;
    private CalculatorHistoryRepository repository;
    private CalculatorConfig calculatorConfig;

    @BeforeEach
    void setUp() {
        repository = new CalculatorHistoryRepository();
        CalculatorSummaryService summaryService = new CalculatorSummaryService(repository);
        calculatorConfig = new CalculatorConfig();
        calculatorConfig.setEnabled(true);
        controller = new CalculatorSummaryController(summaryService, calculatorConfig);
    }

    private void saveEntry(String from, String to, String provider) {
        repository.save(CalculatorHistory.builder()
                .fromCurrency(from).toCurrency(to).amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45")).convertedAmount(new BigDecimal("8345.00"))
                .provider(provider).favorite(false).build());
    }

    @Test
    void getSummary_returnsEmptyWhenNoHistory() {
        CalculatorSummary summary = controller.getSummary();

        assertEquals(0, summary.getTotalConversions());
        assertNotNull(summary.getGeneratedAt());
    }

    @Test
    void getSummary_returnsCorrectData() {
        saveEntry("USD", "INR", "EXCHANGE_RATE_API");
        saveEntry("USD", "EUR", "OPEN_EXCHANGE_RATES");

        CalculatorSummary summary = controller.getSummary();

        assertEquals(2, summary.getTotalConversions());
        assertEquals("USD/INR", summary.getMostUsedPair());
    }

    @Test
    void getSummary_throwsWhenCalculatorDisabled() {
        calculatorConfig.setEnabled(false);

        assertThrows(ResponseStatusException.class, () -> controller.getSummary());
    }

    @Test
    void getPairFrequency_returnsMap() {
        saveEntry("USD", "INR", "EXCHANGE_RATE_API");
        saveEntry("USD", "INR", "EXCHANGE_RATE_API");
        saveEntry("EUR", "GBP", "OPEN_EXCHANGE_RATES");

        Map<String, Long> frequency = controller.getPairFrequency();

        assertEquals(2L, frequency.get("USD/INR"));
        assertEquals(1L, frequency.get("EUR/GBP"));
    }

    @Test
    void getPairFrequency_throwsWhenCalculatorDisabled() {
        calculatorConfig.setEnabled(false);

        assertThrows(ResponseStatusException.class, () -> controller.getPairFrequency());
    }

    @Test
    void getProviderFrequency_returnsMap() {
        saveEntry("USD", "INR", "EXCHANGE_RATE_API");
        saveEntry("EUR", "GBP", "OPEN_EXCHANGE_RATES");

        Map<String, Long> frequency = controller.getProviderFrequency();

        assertEquals(1L, frequency.get("EXCHANGE_RATE_API"));
        assertEquals(1L, frequency.get("OPEN_EXCHANGE_RATES"));
    }

    @Test
    void getProviderFrequency_throwsWhenCalculatorDisabled() {
        calculatorConfig.setEnabled(false);

        assertThrows(ResponseStatusException.class, () -> controller.getProviderFrequency());
    }

    @Test
    void getSummary_includesFavoriteCount() {
        repository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN)
                .rate(new BigDecimal("83.45")).provider("EXCHANGE_RATE_API").favorite(true).build());
        saveEntry("EUR", "GBP", "OPEN_EXCHANGE_RATES");

        CalculatorSummary summary = controller.getSummary();

        assertEquals(2, summary.getTotalConversions());
        assertEquals(1, summary.getFavoriteCount());
    }
}
