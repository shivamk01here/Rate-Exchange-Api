package com.example.exchangerate.pairtag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PairTagServiceTest {

    private PairTagService pairTagService;
    private PairTagRepository pairTagRepository;

    @BeforeEach
    void setUp() {
        pairTagRepository = new PairTagRepository();
        pairTagService = new PairTagService(pairTagRepository);
    }

    @Test
    void createTag_returnsSavedTagWithId() {
        PairTag tag = PairTag.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .tag("travel")
                .build();

        PairTag saved = pairTagService.createTag(tag);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertEquals("travel", saved.getTag());
    }

    @Test
    void getTag_returnsTagWhenExists() {
        PairTag saved = pairTagService.createTag(PairTag.builder()
                .fromCurrency("EUR").toCurrency("GBP")
                .tag("work").build());

        PairTag found = pairTagService.getTag(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getTag_returnsEmptyWhenNotFound() {
        assertTrue(pairTagService.getTag("nonexistent").isEmpty());
    }

    @Test
    void getAllTags_returnsAllCreatedTags() {
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("EUR").tag("travel").build());
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("GBP").toCurrency("JPY").tag("work").build());

        List<PairTag> all = pairTagService.getAllTags();

        assertEquals(2, all.size());
    }

    @Test
    void getTagsByPair_filtersCorrectly() {
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("travel").build());
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("EUR").tag("work").build());

        List<PairTag> result = pairTagService.getTagsByPair("USD", "INR");

        assertEquals(1, result.size());
        assertEquals("INR", result.get(0).getToCurrency());
    }

    @Test
    void getTagsByTag_filtersCorrectly() {
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("travel").build());
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("EUR").toCurrency("GBP").tag("work").build());

        List<PairTag> result = pairTagService.getTagsByTag("travel");

        assertEquals(1, result.size());
    }

    @Test
    void getDistinctTags_returnsUniqueTags() {
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("travel").build());
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("GBP").toCurrency("JPY").tag("travel").build());
        pairTagService.createTag(PairTag.builder()
                .fromCurrency("EUR").toCurrency("GBP").tag("work").build());

        List<String> result = pairTagService.getDistinctTags();

        assertEquals(2, result.size());
    }

    @Test
    void deleteTag_removesTag() {
        PairTag saved = pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("CAD").tag("travel").build());

        assertTrue(pairTagService.deleteTag(saved.getId()));
        assertTrue(pairTagService.getTag(saved.getId()).isEmpty());
    }

    @Test
    void deleteTag_returnsFalseForNonexistent() {
        assertFalse(pairTagService.deleteTag("nonexistent"));
    }

    @Test
    void updateTag_updatesFields() {
        PairTag saved = pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("INR").tag("old-tag").build());

        PairTag updated = pairTagService.updateTag(saved.getId(),
                PairTag.builder().tag("new-tag").build());

        assertEquals("new-tag", updated.getTag());
        assertEquals("USD", updated.getFromCurrency());
    }

    @Test
    void updateTag_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> pairTagService.updateTag("bad-id", PairTag.builder().tag("x").build()));
    }

    @Test
    void getTagCount_returnsCorrectCount() {
        assertEquals(0, pairTagService.getTagCount());

        pairTagService.createTag(PairTag.builder()
                .fromCurrency("USD").toCurrency("EUR").tag("travel").build());

        assertEquals(1, pairTagService.getTagCount());
    }
}
