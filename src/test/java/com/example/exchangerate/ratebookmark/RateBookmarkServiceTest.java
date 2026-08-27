package com.example.exchangerate.ratebookmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RateBookmarkServiceTest {

    private RateBookmarkService rateBookmarkService;
    private RateBookmarkRepository rateBookmarkRepository;

    @BeforeEach
    void setUp() {
        rateBookmarkRepository = new RateBookmarkRepository();
        rateBookmarkService = new RateBookmarkService(rateBookmarkRepository);
    }

    @Test
    void createBookmark_returnsSavedBookmarkWithId() {
        RateBookmark bookmark = RateBookmark.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .label("Trip rate")
                .providerCode("EXCHANGE_RATE_API")
                .build();

        RateBookmark saved = rateBookmarkService.createBookmark(bookmark);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertEquals(0, new BigDecimal("83.45").compareTo(saved.getRate()));
    }

    @Test
    void getBookmark_returnsBookmarkWhenExists() {
        RateBookmark saved = rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("EUR").toCurrency("GBP")
                .rate(new BigDecimal("0.85")).build());

        RateBookmark found = rateBookmarkService.getBookmark(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getBookmark_returnsEmptyWhenNotFound() {
        assertTrue(rateBookmarkService.getBookmark("nonexistent").isEmpty());
    }

    @Test
    void getAllBookmarks_returnsAllCreatedBookmarks() {
        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).build());
        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .rate(new BigDecimal("185.20")).build());

        List<RateBookmark> all = rateBookmarkService.getAllBookmarks();

        assertEquals(2, all.size());
    }

    @Test
    void getBookmarksByPair_filtersCorrectly() {
        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45")).build());
        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).build());

        List<RateBookmark> result = rateBookmarkService.getBookmarksByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getBookmarksByProvider_filtersCorrectly() {
        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45")).providerCode("EXCHANGE_RATE_API").build());
        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).providerCode("OPEN_EXCHANGE_RATES").build());

        List<RateBookmark> result = rateBookmarkService.getBookmarksByProvider("EXCHANGE_RATE_API");

        assertEquals(1, result.size());
    }

    @Test
    void deleteBookmark_removesBookmark() {
        RateBookmark saved = rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("CAD")
                .rate(new BigDecimal("1.35")).build());

        assertTrue(rateBookmarkService.deleteBookmark(saved.getId()));
        assertTrue(rateBookmarkService.getBookmark(saved.getId()).isEmpty());
    }

    @Test
    void deleteBookmark_returnsFalseForNonexistent() {
        assertFalse(rateBookmarkService.deleteBookmark("nonexistent"));
    }

    @Test
    void updateBookmark_updatesFields() {
        RateBookmark saved = rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .label("old label")
                .providerCode("EXCHANGE_RATE_API").build());

        RateBookmark updated = rateBookmarkService.updateBookmark(saved.getId(),
                RateBookmark.builder().rate(new BigDecimal("84.00")).label("new label").build());

        assertEquals("new label", updated.getLabel());
        assertEquals(0, new BigDecimal("84.00").compareTo(updated.getRate()));
        assertEquals("USD", updated.getFromCurrency());
    }

    @Test
    void updateBookmark_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> rateBookmarkService.updateBookmark("bad-id", RateBookmark.builder().rate(new BigDecimal("1.00")).build()));
    }

    @Test
    void getBookmarkCount_returnsCorrectCount() {
        assertEquals(0, rateBookmarkService.getBookmarkCount());

        rateBookmarkService.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).build());

        assertEquals(1, rateBookmarkService.getBookmarkCount());
    }
}
