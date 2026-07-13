package com.example.exchangerate.controllers;

import com.example.exchangerate.favorites.FavoritePair;
import com.example.exchangerate.favorites.FavoritePairService;
import com.example.exchangerate.favorites.FavoriteRateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoritePairController {

    private final FavoritePairService favoritePairService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public FavoritePair createFavorite(@Valid @RequestBody FavoritePair favorite) {
        if (favorite.getFromCurrency() == null || favorite.getFromCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromCurrency is required");
        }
        if (favorite.getToCurrency() == null || favorite.getToCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "toCurrency is required");
        }
        log.info("Creating favorite: {}->{} label={}",
                favorite.getFromCurrency(), favorite.getToCurrency(), favorite.getLabel());
        return favoritePairService.createFavorite(favorite);
    }

    @GetMapping
    public List<FavoritePair> getAllFavorites() {
        return favoritePairService.getAllFavorites();
    }

    @GetMapping("/{id}")
    public FavoritePair getFavorite(@PathVariable String id) {
        return favoritePairService.getFavorite(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<FavoritePair> getFavoritesByPair(@RequestParam String from, @RequestParam String to) {
        return favoritePairService.getFavoritesByCurrencyPair(from, to);
    }

    @GetMapping("/by-label")
    public List<FavoritePair> getFavoritesByLabel(@RequestParam String label) {
        return favoritePairService.getFavoritesByLabel(label);
    }

    @PutMapping("/{id}")
    public FavoritePair updateFavorite(@PathVariable String id, @Valid @RequestBody FavoritePair favorite) {
        try {
            return favoritePairService.updateFavorite(id, favorite);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteFavorite(@PathVariable String id) {
        boolean deleted = favoritePairService.deleteFavorite(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @GetMapping("/rates")
    public CompletableFuture<List<FavoriteRateResult>> getRatesForFavorites() {
        log.info("Fetching rates for all favorite pairs");
        return favoritePairService.fetchRatesForFavorites();
    }
}
