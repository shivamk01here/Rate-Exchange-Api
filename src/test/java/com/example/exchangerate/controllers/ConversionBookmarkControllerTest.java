package com.example.exchangerate.controllers;

import com.example.exchangerate.bookmark.ConversionBookmark;
import com.example.exchangerate.bookmark.ConversionBookmarkRepository;
import com.example.exchangerate.bookmark.ConversionBookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversionBookmarkControllerTest {

    private ConversionBookmarkController controller;

    @BeforeEach
    void setUp() {
        ConversionBookmarkRepository repository = new ConversionBookmarkRepository();
        ConversionBookmarkService service = new ConversionBookmarkService(repository, null);
        controller = new ConversionBookmarkController(service);
    }

    @Test
    void createBookmark_returnsCreatedBookmark() {
        ConversionBookmark bookmark = ConversionBookmark.builder()
                .name("Test Bookmark")
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .build();

        ConversionBookmark result = controller.createBookmark(bookmark);

        assertNotNull(result.getId());
        assertEquals("Test Bookmark", result.getName());
    }

    @Test
    void createBookmark_throwsWhenNameMissing() {
        ConversionBookmark bookmark = ConversionBookmark.builder()
                .fromCurrency("USD").toCurrency("EUR").build();

        assertThrows(ResponseStatusException.class, () -> controller.createBookmark(bookmark));
    }

    @Test
    void getAllBookmarks_returnsAll() {
        controller.createBookmark(ConversionBookmark.builder()
                .name("B1").fromCurrency("USD").toCurrency("EUR").build());
        controller.createBookmark(ConversionBookmark.builder()
                .name("B2").fromCurrency("GBP").toCurrency("USD").build());

        List<ConversionBookmark> all = controller.getAllBookmarks();

        assertEquals(2, all.size());
    }

    @Test
    void getBookmark_returnsById() {
        ConversionBookmark created = controller.createBookmark(ConversionBookmark.builder()
                .name("Find").fromCurrency("USD").toCurrency("JPY").build());

        ConversionBookmark result = controller.getBookmark(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getBookmark_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getBookmark("bad-id"));
    }

    @Test
    void deleteBookmark_returnsSuccess() {
        ConversionBookmark created = controller.createBookmark(ConversionBookmark.builder()
                .name("Delete").fromCurrency("USD").toCurrency("CAD").build());

        Map<String, String> result = controller.deleteBookmark(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void updateBookmark_updatesName() {
        ConversionBookmark created = controller.createBookmark(ConversionBookmark.builder()
                .name("Old").fromCurrency("USD").toCurrency("GBP")
                .amount(new BigDecimal("50")).build());

        ConversionBookmark updated = controller.updateBookmark(created.getId(),
                ConversionBookmark.builder().name("New").build());

        assertEquals("New", updated.getName());
    }

    @Test
    void getBookmarkCount_returnsCount() {
        controller.createBookmark(ConversionBookmark.builder()
                .name("Count").fromCurrency("USD").toCurrency("EUR").build());

        Map<String, Object> result = controller.getBookmarkCount();

        assertEquals(1L, result.get("count"));
    }
}
