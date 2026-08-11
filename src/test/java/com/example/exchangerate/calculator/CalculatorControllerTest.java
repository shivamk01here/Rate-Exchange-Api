package com.example.exchangerate.calculator;

import com.example.exchangerate.controllers.CalculatorController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorControllerTest {

    private CalculatorController controller;
    private CalculatorHistoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CalculatorHistoryRepository();
        CalculatorService service = new CalculatorService(repository, null);
        controller = new CalculatorController(service);
    }

    private CalculatorHistory saveEntry(String from, String to, BigDecimal amount) {
        return repository.save(CalculatorHistory.builder()
                .fromCurrency(from).toCurrency(to).amount(amount)
                .rate(new BigDecimal("83.45")).convertedAmount(new BigDecimal("8345.00"))
                .provider("EXCHANGE_RATE_API").favorite(false).build());
    }

    @Test
    void getAllHistory_returnsAll() {
        saveEntry("USD", "INR", new BigDecimal("100"));
        saveEntry("GBP", "USD", new BigDecimal("50"));

        List<CalculatorHistory> all = controller.getAllHistory();

        assertEquals(2, all.size());
    }

    @Test
    void getHistory_returnsById() {
        CalculatorHistory saved = saveEntry("USD", "EUR", new BigDecimal("50"));

        CalculatorHistory result = controller.getHistory(saved.getId());

        assertEquals(saved.getId(), result.getId());
        assertEquals("USD", result.getFromCurrency());
    }

    @Test
    void getHistory_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getHistory("bad-id"));
    }

    @Test
    void deleteHistory_returnsSuccess() {
        CalculatorHistory saved = saveEntry("GBP", "USD", BigDecimal.TEN);

        Map<String, String> result = controller.deleteHistory(saved.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void deleteHistory_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteHistory("bad-id"));
    }

    @Test
    void clearHistory_removesAll() {
        saveEntry("USD", "JPY", BigDecimal.ONE);
        saveEntry("EUR", "INR", BigDecimal.TEN);

        controller.clearHistory();

        assertEquals(0L, controller.getHistoryCount().get("count"));
    }

    @Test
    void getHistoryCount_returnsCount() {
        saveEntry("USD", "CAD", new BigDecimal("25"));

        Map<String, Object> result = controller.getHistoryCount();

        assertEquals(1L, result.get("count"));
    }

    @Test
    void toggleFavorite_togglesState() {
        CalculatorHistory saved = saveEntry("USD", "AUD", BigDecimal.TEN);

        CalculatorHistory toggled = controller.toggleFavorite(saved.getId());

        assertTrue(toggled.isFavorite());
    }

    @Test
    void toggleFavorite_flipsBackToFalse() {
        CalculatorHistory saved = saveEntry("USD", "AUD", BigDecimal.TEN);
        controller.toggleFavorite(saved.getId());

        CalculatorHistory toggledBack = controller.toggleFavorite(saved.getId());

        assertFalse(toggledBack.isFavorite());
    }

    @Test
    void toggleFavorite_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.toggleFavorite("bad-id"));
    }

    @Test
    void reverse_createsReversedEntry() {
        CalculatorHistory saved = saveEntry("USD", "INR", new BigDecimal("100"));

        CalculatorHistory reversed = controller.reverse(saved.getId());

        assertEquals("INR", reversed.getFromCurrency());
        assertEquals("USD", reversed.getToCurrency());
        assertEquals(new BigDecimal("8345.00"), reversed.getAmount());
    }

    @Test
    void reverse_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.reverse("bad-id"));
    }

    @Test
    void getHistoryByPair_filtersCorrectly() {
        saveEntry("USD", "INR", BigDecimal.TEN);
        saveEntry("USD", "EUR", BigDecimal.TEN);
        saveEntry("USD", "INR", new BigDecimal("20"));

        List<CalculatorHistory> result = controller.getHistoryByPair("USD", "INR");

        assertEquals(2, result.size());
    }

    @Test
    void getFavorites_returnsOnlyFavorited() {
        CalculatorHistory saved = saveEntry("USD", "GBP", BigDecimal.ONE);
        controller.toggleFavorite(saved.getId());
        saveEntry("EUR", "USD", BigDecimal.TEN);

        List<CalculatorHistory> favorites = controller.getFavorites();

        assertEquals(1, favorites.size());
        assertTrue(favorites.get(0).isFavorite());
    }

    @Test
    void getFavorites_returnsEmptyWhenNoneFavorited() {
        saveEntry("USD", "EUR", BigDecimal.TEN);

        List<CalculatorHistory> favorites = controller.getFavorites();

        assertTrue(favorites.isEmpty());
    }
}
