package com.example.exchangerate.aliases;

import com.example.exchangerate.controllers.RateAliasController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RateAliasControllerTest {

    private RateAliasController controller;

    @BeforeEach
    void setUp() {
        RateAliasRepository repository = new RateAliasRepository();
        RateAliasService service = new RateAliasService(repository);
        controller = new RateAliasController(service);
    }

    @Test
    void createAlias_returnsCreatedAlias() {
        RateAlias alias = RateAlias.builder()
                .alias("dollar")
                .fromCurrency("USD")
                .toCurrency("INR")
                .build();

        RateAlias result = controller.createAlias(alias);

        assertNotNull(result.getId());
        assertEquals("dollar", result.getAlias());
    }

    @Test
    void createAlias_throwsWhenAliasBlank() {
        RateAlias alias = RateAlias.builder()
                .alias("").fromCurrency("USD").toCurrency("EUR").build();

        assertThrows(ResponseStatusException.class, () -> controller.createAlias(alias));
    }

    @Test
    void getAllAliases_returnsAll() {
        controller.createAlias(RateAlias.builder()
                .alias("a1").fromCurrency("USD").toCurrency("EUR").build());
        controller.createAlias(RateAlias.builder()
                .alias("a2").fromCurrency("GBP").toCurrency("JPY").build());

        List<RateAlias> all = controller.getAllAliases();

        assertEquals(2, all.size());
    }

    @Test
    void getAlias_returnsById() {
        RateAlias created = controller.createAlias(RateAlias.builder()
                .alias("findme").fromCurrency("USD").toCurrency("CAD").build());

        RateAlias result = controller.getAlias(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getAlias_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getAlias("bad-id"));
    }

    @Test
    void lookupByAlias_findsCorrectly() {
        controller.createAlias(RateAlias.builder()
                .alias("yen").fromCurrency("USD").toCurrency("JPY").build());

        RateAlias result = controller.lookupByAlias("yen");

        assertEquals("USD", result.getFromCurrency());
        assertEquals("JPY", result.getToCurrency());
    }

    @Test
    void lookupByAlias_throwsWhenNotFound() {
        assertThrows(ResponseStatusException.class, () -> controller.lookupByAlias("nope"));
    }

    @Test
    void deleteAlias_returnsSuccess() {
        RateAlias created = controller.createAlias(RateAlias.builder()
                .alias("del").fromCurrency("AUD").toCurrency("NZD").build());

        Map<String, String> result = controller.deleteAlias(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void deleteAlias_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteAlias("nope"));
    }

    @Test
    void getAliasCount_returnsCount() {
        controller.createAlias(RateAlias.builder()
                .alias("count").fromCurrency("USD").toCurrency("EUR").build());

        Map<String, Object> result = controller.getAliasCount();

        assertEquals(1L, result.get("count"));
    }
}
