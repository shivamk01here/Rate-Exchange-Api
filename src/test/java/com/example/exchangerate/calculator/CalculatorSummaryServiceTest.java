package com.example.exchangerate.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorSummaryServiceTest {

    private CalculatorSummaryService summaryService;
    private CalculatorHistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        historyRepository = new CalculatorHistoryRepository();
        summaryService = new CalculatorSummaryService(historyRepository);
    }

    @Test
    void generateSummary_returnsEmptySummaryWhenNoHistory() {
        CalculatorSummary summary = summaryService.generateSummary();

        assertEquals(0, summary.getTotalConversions());
        assertEquals(0, summary.getFavoriteCount());
        assertEquals(BigDecimal.ZERO, summary.getTotalAmountConverted());
        assertEquals(BigDecimal.ZERO, summary.getAverageRate());
        assertNull(summary.getMostUsedPair());
        assertNull(summary.getMostUsedProvider());
        assertTrue(summary.getPairFrequency().isEmpty());
        assertTrue(summary.getProviderFrequency().isEmpty());
        assertTrue(summary.getUniqueCurrencies().isEmpty());
        assertNotNull(summary.getGeneratedAt());
    }

    @Test
    void generateSummary_computesCorrectCounts() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45")).provider("EXCHANGE_RATE_API").favorite(true).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(new BigDecimal("50"))
                .rate(new BigDecimal("0.92")).provider("OPEN_EXCHANGE_RATES").favorite(false).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("GBP").toCurrency("USD").amount(new BigDecimal("25"))
                .rate(new BigDecimal("1.27")).provider("EXCHANGE_RATE_API").favorite(false).build());

        CalculatorSummary summary = summaryService.generateSummary();

        assertEquals(3, summary.getTotalConversions());
        assertEquals(1, summary.getFavoriteCount());
        assertEquals(new BigDecimal("175"), summary.getTotalAmountConverted());
    }

    @Test
    void generateSummary_computesAverageRate() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN)
                .rate(new BigDecimal("83.00")).provider("EXCHANGE_RATE_API").build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN)
                .rate(new BigDecimal("85.00")).provider("EXCHANGE_RATE_API").build());

        CalculatorSummary summary = summaryService.generateSummary();

        assertEquals(new BigDecimal("84.0000"), summary.getAverageRate());
    }

    @Test
    void generateSummary_identifiesMostUsedPair() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("EUR").toCurrency("GBP").amount(BigDecimal.TEN).build());

        CalculatorSummary summary = summaryService.generateSummary();

        assertEquals("USD/INR", summary.getMostUsedPair());
        assertEquals(Map.of("USD/INR", 2L, "EUR/GBP", 1L), summary.getPairFrequency());
    }

    @Test
    void generateSummary_identifiesMostUsedProvider() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN)
                .provider("EXCHANGE_RATE_API").build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN)
                .provider("EXCHANGE_RATE_API").build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("EUR").toCurrency("GBP").amount(BigDecimal.TEN)
                .provider("OPEN_EXCHANGE_RATES").build());

        CalculatorSummary summary = summaryService.generateSummary();

        assertEquals("EXCHANGE_RATE_API", summary.getMostUsedProvider());
        assertEquals(2L, summary.getProviderFrequency().get("EXCHANGE_RATE_API"));
        assertEquals(1L, summary.getProviderFrequency().get("OPEN_EXCHANGE_RATES"));
    }

    @Test
    void generateSummary_collectsUniqueCurrencies() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("EUR").toCurrency("GBP").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.TEN).build());

        CalculatorSummary summary = summaryService.generateSummary();

        assertTrue(summary.getUniqueCurrencies().contains("USD"));
        assertTrue(summary.getUniqueCurrencies().contains("INR"));
        assertTrue(summary.getUniqueCurrencies().contains("EUR"));
        assertTrue(summary.getUniqueCurrencies().contains("GBP"));
        assertEquals(4, summary.getUniqueCurrencies().size());
    }

    @Test
    void getPairFrequency_returnsCorrectMap() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());

        Map<String, Long> frequency = summaryService.getPairFrequency();

        assertEquals(1L, frequency.size());
        assertEquals(2L, frequency.get("USD/INR"));
    }

    @Test
    void getProviderFrequency_excludesNullProviders() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN)
                .provider("EXCHANGE_RATE_API").build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("EUR").toCurrency("GBP").amount(BigDecimal.TEN).build());

        Map<String, Long> frequency = summaryService.getProviderFrequency();

        assertEquals(1, frequency.size());
        assertEquals(1L, frequency.get("EXCHANGE_RATE_API"));
    }

    @Test
    void generateSummary_handlesZeroAmounts() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(null).rate(null).build());

        CalculatorSummary summary = summaryService.generateSummary();

        assertEquals(1, summary.getTotalConversions());
        assertEquals(BigDecimal.ZERO, summary.getTotalAmountConverted());
        assertEquals(BigDecimal.ZERO, summary.getAverageRate());
    }
}
