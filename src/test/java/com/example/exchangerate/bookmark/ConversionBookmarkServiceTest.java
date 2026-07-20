package com.example.exchangerate.bookmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversionBookmarkServiceTest {

    private ConversionBookmarkService bookmarkService;
    private ConversionBookmarkRepository bookmarkRepository;

    @BeforeEach
    void setUp() {
        bookmarkRepository = new ConversionBookmarkRepository();
        bookmarkService = new ConversionBookmarkService(bookmarkRepository, null);
    }

    @Test
    void createBookmark_returnsSavedBookmarkWithId() {
        ConversionBookmark bookmark = ConversionBookmark.builder()
                .name("USD to INR")
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .build();

        ConversionBookmark saved = bookmarkService.createBookmark(bookmark);

        assertNotNull(saved.getId());
        assertEquals("USD to INR", saved.getName());
        assertEquals("USD", saved.getFromCurrency());
    }

    @Test
    void getBookmark_returnsBookmarkWhenExists() {
        ConversionBookmark saved = bookmarkService.createBookmark(ConversionBookmark.builder()
                .name("EUR to USD").fromCurrency("EUR").toCurrency("USD")
                .amount(BigDecimal.ONE).build());

        ConversionBookmark found = bookmarkService.getBookmark(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getBookmark_returnsEmptyWhenNotFound() {
        assertTrue(bookmarkService.getBookmark("nonexistent").isEmpty());
    }

    @Test
    void getAllBookmarks_returnsAllCreatedBookmarks() {
        bookmarkService.createBookmark(ConversionBookmark.builder()
                .name("B1").fromCurrency("USD").toCurrency("EUR").build());
        bookmarkService.createBookmark(ConversionBookmark.builder()
                .name("B2").fromCurrency("GBP").toCurrency("USD").build());

        List<ConversionBookmark> all = bookmarkService.getAllBookmarks();

        assertEquals(2, all.size());
    }

    @Test
    void deleteBookmark_removesBookmark() {
        ConversionBookmark saved = bookmarkService.createBookmark(ConversionBookmark.builder()
                .name("Delete").fromCurrency("USD").toCurrency("JPY").build());

        assertTrue(bookmarkService.deleteBookmark(saved.getId()));
        assertTrue(bookmarkService.getBookmark(saved.getId()).isEmpty());
    }

    @Test
    void deleteBookmark_returnsFalseForNonexistent() {
        assertFalse(bookmarkService.deleteBookmark("nonexistent"));
    }

    @Test
    void updateBookmark_updatesFields() {
        ConversionBookmark saved = bookmarkService.createBookmark(ConversionBookmark.builder()
                .name("Old Name").fromCurrency("USD").toCurrency("INR")
                .amount(new BigDecimal("100")).build());

        ConversionBookmark updated = bookmarkService.updateBookmark(saved.getId(),
                ConversionBookmark.builder().name("Updated Name").build());

        assertEquals("Updated Name", updated.getName());
        assertEquals("USD", updated.getFromCurrency());
    }

    @Test
    void getBookmarkCount_returnsCorrectCount() {
        assertEquals(0, bookmarkService.getBookmarkCount());

        bookmarkService.createBookmark(ConversionBookmark.builder()
                .name("Count").fromCurrency("USD").toCurrency("EUR").build());

        assertEquals(1, bookmarkService.getBookmarkCount());
    }

    @Test
    void executeBookmark_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> bookmarkService.executeBookmark("nonexistent"));
    }
}
