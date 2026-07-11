package com.example.exchangerate.favorites;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritePairService {

    private final FavoritePairRepository favoritePairRepository;

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
}
