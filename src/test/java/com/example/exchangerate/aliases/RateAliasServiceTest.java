package com.example.exchangerate.aliases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RateAliasServiceTest {

    private RateAliasService aliasService;
    private RateAliasRepository aliasRepository;

    @BeforeEach
    void setUp() {
        aliasRepository = new RateAliasRepository();
        aliasService = new RateAliasService(aliasRepository);
    }

    @Test
    void createAlias_returnsSavedAliasWithId() {
        RateAlias alias = RateAlias.builder()
                .alias("dollar")
                .fromCurrency("USD")
                .toCurrency("INR")
                .build();

        RateAlias saved = aliasService.createAlias(alias);

        assertNotNull(saved.getId());
        assertEquals("dollar", saved.getAlias());
        assertEquals("USD", saved.getFromCurrency());
    }

    @Test
    void createAlias_storesLowercaseAlias() {
        RateAlias saved = aliasService.createAlias(RateAlias.builder()
                .alias("EuroToYen").fromCurrency("EUR").toCurrency("JPY").build());

        RateAlias found = aliasService.lookupByAlias("eurotoyen").orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getAlias_returnsAliasWhenExists() {
        RateAlias saved = aliasService.createAlias(RateAlias.builder()
                .alias("pound").fromCurrency("GBP").toCurrency("USD").build());

        RateAlias found = aliasService.getAlias(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("pound", found.getAlias());
    }

    @Test
    void getAlias_returnsEmptyWhenNotFound() {
        assertTrue(aliasService.getAlias("nonexistent").isEmpty());
    }

    @Test
    void lookupByAlias_findsByAliasName() {
        aliasService.createAlias(RateAlias.builder()
                .alias("yen-rate").fromCurrency("USD").toCurrency("JPY").build());

        RateAlias found = aliasService.lookupByAlias("yen-rate").orElse(null);

        assertNotNull(found);
        assertEquals("USD", found.getFromCurrency());
    }

    @Test
    void lookupByAlias_returnsEmptyWhenNoMatch() {
        assertTrue(aliasService.lookupByAlias("nope").isEmpty());
    }

    @Test
    void getAllAliases_returnsAllCreated() {
        aliasService.createAlias(RateAlias.builder()
                .alias("a1").fromCurrency("USD").toCurrency("EUR").build());
        aliasService.createAlias(RateAlias.builder()
                .alias("a2").fromCurrency("GBP").toCurrency("JPY").build());

        List<RateAlias> all = aliasService.getAllAliases();

        assertEquals(2, all.size());
    }

    @Test
    void deleteAlias_removesAlias() {
        RateAlias saved = aliasService.createAlias(RateAlias.builder()
                .alias("temp").fromCurrency("CAD").toCurrency("AUD").build());

        assertTrue(aliasService.deleteAlias(saved.getId()));
        assertTrue(aliasService.getAlias(saved.getId()).isEmpty());
    }

    @Test
    void deleteAlias_returnsFalseForNonexistent() {
        assertFalse(aliasService.deleteAlias("nonexistent"));
    }

    @Test
    void getAliasCount_returnsCorrectCount() {
        assertEquals(0, aliasService.getAliasCount());

        aliasService.createAlias(RateAlias.builder()
                .alias("count-test").fromCurrency("USD").toCurrency("INR").build());

        assertEquals(1, aliasService.getAliasCount());
    }
}
