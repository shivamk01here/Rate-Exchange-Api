package com.example.exchangerate.favorites;

import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoritePairServiceTest {

    private FavoritePairService favoritePairService;
    private FavoritePairRepository favoritePairRepository;

    @Mock
    private ExchangeRateOrchestrationService orchestrationService;

    @BeforeEach
    void setUp() {
        favoritePairRepository = new FavoritePairRepository();
        favoritePairService = new FavoritePairService(favoritePairRepository, orchestrationService);
    }

    @Test
    void createFavorite_returnsSavedFavoriteWithId() {
        FavoritePair favorite = FavoritePair.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .label("My Travel Pair")
                .build();

        FavoritePair saved = favoritePairService.createFavorite(favorite);

        assertNotNull(saved.getId());
        assertEquals("USD", saved.getFromCurrency());
        assertEquals("INR", saved.getToCurrency());
        assertEquals("My Travel Pair", saved.getLabel());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void getFavorite_returnsFavoriteWhenExists() {
        FavoritePair favorite = FavoritePair.builder()
                .fromCurrency("EUR")
                .toCurrency("GBP")
                .label("Euro Watch")
                .build();
        FavoritePair saved = favoritePairService.createFavorite(favorite);

        FavoritePair found = favoritePairService.getFavorite(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("EUR", found.getFromCurrency());
    }

    @Test
    void getFavorite_returnsEmptyWhenNotFound() {
        assertTrue(favoritePairService.getFavorite("nonexistent").isEmpty());
    }

    @Test
    void getAllFavorites_returnsAllCreatedFavorites() {
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("EUR").label("Pair 1").build());
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("GBP").toCurrency("JPY").label("Pair 2").build());

        List<FavoritePair> all = favoritePairService.getAllFavorites();

        assertEquals(2, all.size());
    }

    @Test
    void deleteFavorite_removesFavorite() {
        FavoritePair saved = favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("CAD").label("Delete Me").build());

        assertTrue(favoritePairService.deleteFavorite(saved.getId()));
        assertTrue(favoritePairService.getFavorite(saved.getId()).isEmpty());
    }

    @Test
    void deleteFavorite_returnsFalseForNonexistent() {
        assertFalse(favoritePairService.deleteFavorite("nonexistent"));
    }

    @Test
    void updateFavorite_updatesFields() {
        FavoritePair saved = favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("EUR").label("Old Label").build());

        FavoritePair updated = favoritePairService.updateFavorite(saved.getId(),
                FavoritePair.builder().label("New Label").build());

        assertEquals("New Label", updated.getLabel());
        assertEquals("USD", updated.getFromCurrency());
        assertEquals("EUR", updated.getToCurrency());
    }

    @Test
    void updateFavorite_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> favoritePairService.updateFavorite("bad-id",
                        FavoritePair.builder().label("test").build()));
    }

    @Test
    void getFavoritesByCurrencyPair_returnsMatchingFavorites() {
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("INR").label("Travel 1").build());
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("INR").label("Travel 2").build());
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("EUR").toCurrency("USD").label("Euro").build());

        List<FavoritePair> usdInr = favoritePairService.getFavoritesByCurrencyPair("USD", "INR");

        assertEquals(2, usdInr.size());
    }

    @Test
    void getFavoritesByLabel_returnsMatchingFavorites() {
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("GBP").label("Work").build());
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("EUR").toCurrency("GBP").label("Work").build());

        List<FavoritePair> byLabel = favoritePairService.getFavoritesByLabel("Work");

        assertEquals(2, byLabel.size());
    }

    @Test
    void getFavoritesByLabel_skipsFavoritesWithoutLabel() {
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("GBP").label("Work").build());
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("EUR").toCurrency("GBP").build());

        List<FavoritePair> byLabel = favoritePairService.getFavoritesByLabel("Work");

        assertEquals(1, byLabel.size());
        assertEquals(0, favoritePairService.getFavoritesByLabel(null).size());
    }

    @Test
    void getFavoriteCount_returnsCorrectCount() {
        assertEquals(0, favoritePairService.getFavoriteCount());

        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("USD").toCurrency("EUR").build());
        favoritePairService.createFavorite(FavoritePair.builder()
                .fromCurrency("GBP").toCurrency("JPY").build());

        assertEquals(2, favoritePairService.getFavoriteCount());
    }
}
