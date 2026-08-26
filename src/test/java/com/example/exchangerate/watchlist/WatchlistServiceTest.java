package com.example.exchangerate.watchlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistServiceTest {

    private WatchlistService watchlistService;
    private WatchlistRepository watchlistRepository;

    @BeforeEach
    void setUp() {
        watchlistRepository = new WatchlistRepository();
        watchlistService = new WatchlistService(watchlistRepository);
    }

    @Test
    void createEntry_returnsSavedEntryWithId() {
        WatchlistEntry entry = WatchlistEntry.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .label("Trip rates")
                .priority(WatchlistPriority.HIGH)
                .enabled(true)
                .build();

        WatchlistEntry saved = watchlistService.createEntry(entry);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertEquals(WatchlistPriority.HIGH, saved.getPriority());
        assertTrue(saved.isEnabled());
    }

    @Test
    void getEntry_returnsEntryWhenExists() {
        WatchlistEntry saved = watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("EUR").toCurrency("GBP")
                .priority(WatchlistPriority.MEDIUM).enabled(true).build());

        WatchlistEntry found = watchlistService.getEntry(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getEntry_returnsEmptyWhenNotFound() {
        assertTrue(watchlistService.getEntry("nonexistent").isEmpty());
    }

    @Test
    void getAllEntries_returnsAllCreatedEntries() {
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .priority(WatchlistPriority.LOW).enabled(true).build());

        List<WatchlistEntry> all = watchlistService.getAllEntries();

        assertEquals(2, all.size());
    }

    @Test
    void getEntriesByPair_filtersCorrectly() {
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .priority(WatchlistPriority.LOW).enabled(true).build());
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.MEDIUM).enabled(true).build());

        List<WatchlistEntry> result = watchlistService.getEntriesByPair("USD", "INR");

        assertEquals(2, result.size());
    }

    @Test
    void getEntriesByPriority_filtersCorrectly() {
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .priority(WatchlistPriority.LOW).enabled(true).build());

        List<WatchlistEntry> result = watchlistService.getEntriesByPriority(WatchlistPriority.HIGH);

        assertEquals(1, result.size());
    }

    @Test
    void getEnabledEntries_filtersCorrectly() {
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());
        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .priority(WatchlistPriority.LOW).enabled(false).build());

        List<WatchlistEntry> result = watchlistService.getEnabledEntries();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isEnabled());
    }

    @Test
    void deleteEntry_removesEntry() {
        WatchlistEntry saved = watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("CAD")
                .priority(WatchlistPriority.MEDIUM).enabled(true).build());

        assertTrue(watchlistService.deleteEntry(saved.getId()));
        assertTrue(watchlistService.getEntry(saved.getId()).isEmpty());
    }

    @Test
    void deleteEntry_returnsFalseForNonexistent() {
        assertFalse(watchlistService.deleteEntry("nonexistent"));
    }

    @Test
    void updateEntry_updatesFields() {
        WatchlistEntry saved = watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .label("old label")
                .priority(WatchlistPriority.HIGH).enabled(true).build());

        WatchlistEntry updated = watchlistService.updateEntry(saved.getId(),
                WatchlistEntry.builder().label("new label").priority(WatchlistPriority.LOW).build());

        assertEquals("new label", updated.getLabel());
        assertEquals(WatchlistPriority.LOW, updated.getPriority());
        assertEquals("USD", updated.getFromCurrency());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void updateEntry_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> watchlistService.updateEntry("bad-id", WatchlistEntry.builder().label("x").build()));
    }

    @Test
    void toggleEnabled_togglesTheState() {
        WatchlistEntry saved = watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("INR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());

        WatchlistEntry toggled = watchlistService.toggleEnabled(saved.getId());

        assertFalse(toggled.isEnabled());
        assertNotNull(toggled.getUpdatedAt());
    }

    @Test
    void toggleEnabled_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> watchlistService.toggleEnabled("bad-id"));
    }

    @Test
    void getEntryCount_returnsCorrectCount() {
        assertEquals(0, watchlistService.getEntryCount());

        watchlistService.createEntry(WatchlistEntry.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .priority(WatchlistPriority.HIGH).enabled(true).build());

        assertEquals(1, watchlistService.getEntryCount());
    }
}
