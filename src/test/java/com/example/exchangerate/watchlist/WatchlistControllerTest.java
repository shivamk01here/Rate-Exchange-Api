package com.example.exchangerate.watchlist;

import com.example.exchangerate.controllers.WatchlistController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistControllerTest {

    private WatchlistController controller;

    @BeforeEach
    void setUp() {
        WatchlistRepository repository = new WatchlistRepository();
        WatchlistService service = new WatchlistService(repository);
        controller = new WatchlistController(service);
    }

    @Test
    void createEntry_returnsCreatedEntry() {
        WatchlistEntry entry = WatchlistEntry.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .label("Trip rates")
                .priority(WatchlistPriority.HIGH)
                .enabled(true)
                .build();

        WatchlistEntry result = controller.createEntry(entry);

        assertNotNull(result.getId());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("INR", result.getToCurrency());
        assertEquals(WatchlistPriority.HIGH, result.getPriority());
    }

    @Test
    void getAllEntries_returnsAll() {
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .priority(WatchlistPriority.LOW).enabled(true).build());

        List<WatchlistEntry> all = controller.getAllEntries();

        assertEquals(2, all.size());
    }

    @Test
    void getEntry_returnsById() {
        WatchlistEntry created = controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("CAD")
                .priority(WatchlistPriority.MEDIUM).enabled(true).build());

        WatchlistEntry result = controller.getEntry(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getEntry_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getEntry("bad-id"));
    }

    @Test
    void getEntriesByPair_filtersCorrectly() {
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .priority(WatchlistPriority.LOW).enabled(true).build());

        List<WatchlistEntry> result = controller.getEntriesByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getEntriesByPriority_filtersCorrectly() {
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .priority(WatchlistPriority.LOW).enabled(true).build());

        List<WatchlistEntry> result = controller.getEntriesByPriority(WatchlistPriority.HIGH);

        assertEquals(1, result.size());
    }

    @Test
    void deleteEntry_returnsSuccess() {
        WatchlistEntry created = controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .priority(WatchlistPriority.MEDIUM).enabled(true).build());

        Map<String, String> result = controller.deleteEntry(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void deleteEntry_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteEntry("nope"));
    }

    @Test
    void toggleEntry_togglesEnabled() {
        WatchlistEntry created = controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());

        WatchlistEntry toggled = controller.toggleEntry(created.getId());

        assertFalse(toggled.isEnabled());
    }

    @Test
    void getEntryCount_returnsCount() {
        controller.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .priority(WatchlistPriority.LOW).enabled(true).build());

        Map<String, Object> result = controller.getEntryCount();

        assertEquals(1L, result.get("count"));
    }
}
