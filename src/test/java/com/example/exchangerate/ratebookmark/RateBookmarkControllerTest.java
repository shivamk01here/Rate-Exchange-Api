package com.example.exchangerate.ratebookmark;

import com.example.exchangerate.controllers.RateBookmarkController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RateBookmarkControllerTest {

    private RateBookmarkController controller;

    @BeforeEach
    void setUp() {
        RateBookmarkRepository repository = new RateBookmarkRepository();
        RateBookmarkService service = new RateBookmarkService(repository);
        controller = new RateBookmarkController(service);
    }

    @Test
    void createBookmark_returnsCreatedBookmark() {
        RateBookmark bookmark = RateBookmark.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .label("Trip rate")
                .providerCode("EXCHANGE_RATE_API")
                .build();

        RateBookmark result = controller.createBookmark(bookmark);

        assertNotNull(result.getId());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("INR", result.getToCurrency());
        assertEquals(0, new BigDecimal("83.45").compareTo(result.getRate()));
    }

    @Test
    void getAllBookmarks_returnsAll() {
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).build());
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("GBP").toCurrency("JPY")
                .rate(new BigDecimal("185.20")).build());

        List<RateBookmark> all = controller.getAllBookmarks();

        assertEquals(2, all.size());
    }

    @Test
    void getBookmark_returnsById() {
        RateBookmark created = controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("CAD")
                .rate(new BigDecimal("1.35")).build());

        RateBookmark result = controller.getBookmark(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getBookmark_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getBookmark("bad-id"));
    }

    @Test
    void getBookmarksByPair_filtersCorrectly() {
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45")).build());
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).build());

        List<RateBookmark> result = controller.getBookmarksByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getBookmarksByProvider_filtersCorrectly() {
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45")).providerCode("EXCHANGE_RATE_API").build());
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).providerCode("OPEN_EXCHANGE_RATES").build());

        List<RateBookmark> result = controller.getBookmarksByProvider("EXCHANGE_RATE_API");

        assertEquals(1, result.size());
    }

    @Test
    void deleteBookmark_returnsSuccess() {
        RateBookmark created = controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .rate(new BigDecimal("0.79")).build());

        Map<String, String> result = controller.deleteBookmark(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void deleteBookmark_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteBookmark("nope"));
    }

    @Test
    void updateBookmark_updatesRate() {
        RateBookmark created = controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45")).build());

        RateBookmark updated = controller.updateBookmark(created.getId(),
                RateBookmark.builder().rate(new BigDecimal("84.00")).build());

        assertEquals(0, new BigDecimal("84.00").compareTo(updated.getRate()));
    }

    @Test
    void getBookmarkCount_returnsCount() {
        controller.createBookmark(RateBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92")).build());

        Map<String, Object> result = controller.getBookmarkCount();

        assertEquals(1L, result.get("count"));
    }
}
