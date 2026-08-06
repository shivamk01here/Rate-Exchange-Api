package com.example.exchangerate.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private CalculatorService calculatorService;
    private CalculatorHistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        historyRepository = new CalculatorHistoryRepository();
        calculatorService = new CalculatorService(historyRepository, null);
    }

    @Test
    void getHistory_returnsEntryWhenExists() {
        CalculatorHistory saved = historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45")).convertedAmount(new BigDecimal("8345.00"))
                .provider("EXCHANGE_RATE_API").build());

        CalculatorHistory found = calculatorService.getHistory(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("USD", found.getFromCurrency());
        assertEquals("INR", found.getToCurrency());
    }

    @Test
    void getHistory_returnsEmptyWhenNotFound() {
        assertTrue(calculatorService.getHistory("nonexistent").isEmpty());
    }

    @Test
    void getAllHistory_returnsAllEntries() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("GBP").toCurrency("USD").amount(new BigDecimal("50")).build());

        List<CalculatorHistory> all = calculatorService.getAllHistory();

        assertEquals(2, all.size());
    }

    @Test
    void getHistoryByPair_filtersCorrectly() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.TEN).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR").amount(new BigDecimal("20")).build());

        List<CalculatorHistory> result = calculatorService.getHistoryByPair("USD", "INR");

        assertEquals(2, result.size());
    }

    @Test
    void toggleFavorite_flipsFavoriteState() {
        CalculatorHistory saved = historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("GBP").amount(BigDecimal.ONE)
                .favorite(false).build());

        CalculatorHistory toggled = calculatorService.toggleFavorite(saved.getId());

        assertTrue(toggled.isFavorite());

        CalculatorHistory toggledAgain = calculatorService.toggleFavorite(saved.getId());

        assertFalse(toggledAgain.isFavorite());
    }

    @Test
    void toggleFavorite_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.toggleFavorite("nonexistent"));
    }

    @Test
    void reverse_createsReversedEntry() {
        CalculatorHistory saved = historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("INR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .provider("EXCHANGE_RATE_API").build());

        CalculatorHistory reversed = calculatorService.reverse(saved.getId());

        assertEquals("INR", reversed.getFromCurrency());
        assertEquals("USD", reversed.getToCurrency());
        assertEquals(new BigDecimal("100"), reversed.getAmount());
        assertEquals(new BigDecimal("8345.00"), reversed.getConvertedAmount());
    }

    @Test
    void reverse_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.reverse("nonexistent"));
    }

    @Test
    void deleteHistory_removesEntry() {
        CalculatorHistory saved = historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("JPY").amount(BigDecimal.ONE).build());

        assertTrue(calculatorService.deleteHistory(saved.getId()));
        assertTrue(calculatorService.getHistory(saved.getId()).isEmpty());
    }

    @Test
    void deleteHistory_returnsFalseForNonexistent() {
        assertFalse(calculatorService.deleteHistory("nonexistent"));
    }

    @Test
    void clearHistory_removesAllEntries() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("GBP").toCurrency("USD").amount(BigDecimal.ONE).build());

        calculatorService.clearHistory();

        assertEquals(0, calculatorService.getHistoryCount());
    }

    @Test
    void getHistoryCount_returnsCorrectCount() {
        assertEquals(0, calculatorService.getHistoryCount());

        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build());

        assertEquals(1, calculatorService.getHistoryCount());
    }

    @Test
    void getFavorites_returnsOnlyFavoritedEntries() {
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).favorite(true).build());
        historyRepository.save(CalculatorHistory.builder()
                .fromCurrency("GBP").toCurrency("USD").amount(BigDecimal.ONE).favorite(false).build());

        List<CalculatorHistory> favorites = calculatorService.getFavorites();

        assertEquals(1, favorites.size());
        assertTrue(favorites.get(0).isFavorite());
    }

    @Test
    void recalculate_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> calculatorService.recalculate("nonexistent"));
    }
}
