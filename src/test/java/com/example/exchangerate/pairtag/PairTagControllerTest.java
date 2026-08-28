package com.example.exchangerate.pairtag;

import com.example.exchangerate.controllers.PairTagController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PairTagControllerTest {

    private PairTagController controller;

    @BeforeEach
    void setUp() {
        PairTagRepository repository = new PairTagRepository();
        PairTagService service = new PairTagService(repository);
        controller = new PairTagController(service);
    }

    @Test
    void createTag_returnsCreatedTag() {
        PairTag tag = PairTag.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .tag("travel")
                .build();

        PairTag result = controller.createTag(tag);

        assertNotNull(result.getId());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("travel", result.getTag());
    }

    @Test
    void getAllTags_returnsAll() {
        controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("EUR").tag("travel").build());
        controller.createTag(PairTag.builder()
                .fromCurrency("GBP").toCurrency("JPY").tag("work").build());

        List<PairTag> all = controller.getAllTags();

        assertEquals(2, all.size());
    }

    @Test
    void getTag_returnsById() {
        PairTag created = controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("CAD").tag("travel").build());

        PairTag result = controller.getTag(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getTag_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getTag("bad-id"));
    }

    @Test
    void getTagsByPair_filtersCorrectly() {
        controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("travel").build());
        controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("EUR").tag("work").build());

        List<PairTag> result = controller.getTagsByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getTagsByTag_filtersCorrectly() {
        controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("travel").build());
        controller.createTag(PairTag.builder()
                .fromCurrency("GBP").toCurrency("JPY").tag("work").build());

        List<PairTag> result = controller.getTagsByTag("travel");

        assertEquals(1, result.size());
    }

    @Test
    void getDistinctTags_returnsUniqueTags() {
        controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("travel").build());
        controller.createTag(PairTag.builder()
                .fromCurrency("GBP").toCurrency("JPY").tag("travel").build());
        controller.createTag(PairTag.builder()
                .fromCurrency("EUR").toCurrency("GBP").tag("work").build());

        List<String> result = controller.getDistinctTags();

        assertEquals(2, result.size());
        assertTrue(result.contains("travel"));
        assertTrue(result.contains("work"));
    }

    @Test
    void deleteTag_returnsSuccess() {
        PairTag created = controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("GBP").tag("travel").build());

        Map<String, String> result = controller.deleteTag(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void deleteTag_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteTag("nope"));
    }

    @Test
    void updateTag_updatesTag() {
        PairTag created = controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("old-tag").build());

        PairTag updated = controller.updateTag(created.getId(),
                PairTag.builder().tag("new-tag").build());

        assertEquals("new-tag", updated.getTag());
    }

    @Test
    void getTagCount_returnsCount() {
        controller.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("EUR").tag("travel").build());

        Map<String, Object> result = controller.getTagCount();

        assertEquals(1L, result.get("count"));
    }
}
