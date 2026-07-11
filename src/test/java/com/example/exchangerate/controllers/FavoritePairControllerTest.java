package com.example.exchangerate.controllers;

import com.example.exchangerate.favorites.FavoritePair;
import com.example.exchangerate.favorites.FavoritePairRepository;
import com.example.exchangerate.favorites.FavoritePairService;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FavoritePairControllerTest {

    private FavoritePairController controller;

    @BeforeEach
    void setUp() {
        FavoritePairRepository repository = new FavoritePairRepository();
        ExchangeRateOrchestrationService orchestrationService = mock(ExchangeRateOrchestrationService.class);
        FavoritePairService service = new FavoritePairService(repository, orchestrationService);
        controller = new FavoritePairController(service);
    }

    @Test
    void createFavorite_returnsCreatedFavorite() {
        FavoritePair favorite = FavoritePair.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .label("Travel Pair")
                .build();

        FavoritePair result = controller.createFavorite(favorite);

        assertNotNull(result.getId());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("INR", result.getToCurrency());
        assertEquals("Travel Pair", result.getLabel());
    }

    @Test
    void createFavorite_throwsWhenFromCurrencyMissing() {
        FavoritePair favorite = FavoritePair.builder()
                .toCurrency("EUR")
                .label("Missing from")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createFavorite(favorite));
    }

    @Test
    void createFavorite_throwsWhenToCurrencyMissing() {
        FavoritePair favorite = FavoritePair.builder()
                .fromCurrency("USD")
                .label("Missing to")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createFavorite(favorite));
    }

    @Test
    void getAllFavorites_returnsAllFavorites() {
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("EUR").label("Pair 1").build());
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("GBP").toCurrency("JPY").label("Pair 2").build());

        List<FavoritePair> all = controller.getAllFavorites();

        assertEquals(2, all.size());
    }

    @Test
    void getFavorite_returnsFavoriteById() {
        FavoritePair created = controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("CAD").label("Canada").build());

        FavoritePair result = controller.getFavorite(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals("Canada", result.getLabel());
    }

    @Test
    void getFavorite_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getFavorite("bad-id"));
    }

    @Test
    void updateFavorite_updatesExistingFavorite() {
        FavoritePair created = controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("EUR").label("Old Label").build());

        FavoritePair updated = controller.updateFavorite(created.getId(),
                FavoritePair.builder().label("New Label").build());

        assertEquals("New Label", updated.getLabel());
        assertEquals("USD", updated.getFromCurrency());
    }

    @Test
    void updateFavorite_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class,
                () -> controller.updateFavorite("bad-id", FavoritePair.builder().build()));
    }

    @Test
    void deleteFavorite_returnsSuccess() {
        FavoritePair created = controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("GBP").label("Delete Me").build());

        Map<String, String> result = controller.deleteFavorite(created.getId());

        assertEquals("deleted", result.get("status"));
        assertThrows(ResponseStatusException.class, () -> controller.getFavorite(created.getId()));
    }

    @Test
    void deleteFavorite_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteFavorite("bad-id"));
    }

    @Test
    void getFavoritesByPair_returnsFilteredFavorites() {
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("INR").label("Pair 1").build());
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("INR").label("Pair 2").build());
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("EUR").toCurrency("USD").label("Euro").build());

        List<FavoritePair> filtered = controller.getFavoritesByPair("USD", "INR");

        assertEquals(2, filtered.size());
    }

    @Test
    void getFavoritesByLabel_returnsFilteredFavorites() {
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("GBP").label("Work").build());
        controller.createFavorite(FavoritePair.builder()
                .fromCurrency("EUR").toCurrency("GBP").label("Work").build());

        List<FavoritePair> byLabel = controller.getFavoritesByLabel("Work");

        assertEquals(2, byLabel.size());
    }
}
