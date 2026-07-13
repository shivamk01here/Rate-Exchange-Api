package com.example.exchangerate.favorites;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritePairService {

    private final FavoritePairRepository favoritePairRepository;
    private final ExchangeRateOrchestrationService orchestrationService;

    public FavoritePair createFavorite(FavoritePair favorite) {
        FavoritePair saved = favoritePairRepository.save(favorite);
        log.info("Favorite created: id={} {}->{} label={}",
                saved.getId(), saved.getFromCurrency(), saved.getToCurrency(), saved.getLabel());
        return saved;
    }

    public Optional<FavoritePair> getFavorite(String id) {
        return favoritePairRepository.findById(id);
    }

    public List<FavoritePair> getAllFavorites() {
        return favoritePairRepository.findAll();
    }

    public List<FavoritePair> getFavoritesByCurrencyPair(String from, String to) {
        return favoritePairRepository.findByCurrencyPair(from, to);
    }

    public List<FavoritePair> getFavoritesByLabel(String label) {
        return favoritePairRepository.findByLabel(label);
    }

    public boolean deleteFavorite(String id) {
        boolean deleted = favoritePairRepository.deleteById(id);
        if (deleted) {
            log.info("Favorite deleted: id={}", id);
        }
        return deleted;
    }

    public FavoritePair updateFavorite(String id, FavoritePair updated) {
        return favoritePairRepository.findById(id)
                .map(existing -> {
                    FavoritePair merged = FavoritePair.builder()
                            .id(existing.getId())
                            .fromCurrency(updated.getFromCurrency() != null ? updated.getFromCurrency() : existing.getFromCurrency())
                            .toCurrency(updated.getToCurrency() != null ? updated.getToCurrency() : existing.getToCurrency())
                            .label(updated.getLabel() != null ? updated.getLabel() : existing.getLabel())
                            .createdAt(existing.getCreatedAt())
                            .build();
                    FavoritePair saved = favoritePairRepository.save(merged);
                    log.info("Favorite updated: id={} {}->{}", id, saved.getFromCurrency(), saved.getToCurrency());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found: " + id));
    }

    public long getFavoriteCount() {
        return favoritePairRepository.count();
    }

    public CompletableFuture<List<FavoriteRateResult>> fetchRatesForFavorites() {
        List<FavoritePair> allFavorites = favoritePairRepository.findAll();
        log.info("Fetching rates for {} favorite pairs", allFavorites.size());

        List<CompletableFuture<FavoriteRateResult>> futures = allFavorites.stream()
                .map(fav -> {
                    ExchangeRateRequest request = ExchangeRateRequest.builder()
                            .fromCurrency(fav.getFromCurrency())
                            .toCurrency(fav.getToCurrency())
                            .build();
                    return orchestrationService.getRate(request)
                            .thenApply(response -> FavoriteRateResult.builder()
                                    .favoriteId(fav.getId())
                                    .fromCurrency(fav.getFromCurrency())
                                    .toCurrency(fav.getToCurrency())
                                    .label(fav.getLabel())
                                    .rate(response.getRate())
                                    .status(response.getStatus())
                                    .provider(response.getProviderCode() != null ? response.getProviderCode().name() : null)
                                    .build())
                            .exceptionally(e -> {
                                log.warn("Failed to fetch rate for favorite {}: {}", fav.getId(), e.getMessage());
                                return FavoriteRateResult.builder()
                                        .favoriteId(fav.getId())
                                        .fromCurrency(fav.getFromCurrency())
                                        .toCurrency(fav.getToCurrency())
                                        .label(fav.getLabel())
                                        .status("FAILED")
                                        .build();
                            });
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
