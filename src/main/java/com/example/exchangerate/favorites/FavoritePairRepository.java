package com.example.exchangerate.favorites;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FavoritePairRepository {

    private final ConcurrentHashMap<String, FavoritePair> favorites = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<FavoritePair> favoriteList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public FavoritePair save(FavoritePair favorite) {
        String id = favorite.getId() != null ? favorite.getId() : String.valueOf(idCounter.incrementAndGet());
        FavoritePair stored = FavoritePair.builder()
                .id(id)
                .fromCurrency(favorite.getFromCurrency())
                .toCurrency(favorite.getToCurrency())
                .label(favorite.getLabel())
                .createdAt(favorite.getCreatedAt() != null ? favorite.getCreatedAt() : java.time.Instant.now())
                .build();

        if (favorites.putIfAbsent(id, stored) == null) {
            favoriteList.add(stored);
        } else {
            favorites.put(id, stored);
            int index = -1;
            for (int i = 0; i < favoriteList.size(); i++) {
                if (id.equals(favoriteList.get(i).getId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                favoriteList.set(index, stored);
            }
        }

        log.debug("FavoritePair saved: id={} {}->{}", id, stored.getFromCurrency(), stored.getToCurrency());
        return stored;
    }

    public Optional<FavoritePair> findById(String id) {
        return Optional.ofNullable(favorites.get(id));
    }

    public List<FavoritePair> findAll() {
        return new ArrayList<>(favoriteList);
    }

    public List<FavoritePair> findByCurrencyPair(String from, String to) {
        return favoriteList.stream()
                .filter(f -> from.equalsIgnoreCase(f.getFromCurrency())
                        && to.equalsIgnoreCase(f.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<FavoritePair> findByLabel(String label) {
        return favoriteList.stream()
                .filter(f -> label.equalsIgnoreCase(f.getLabel()))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        FavoritePair removed = favorites.remove(id);
        if (removed != null) {
            favoriteList.remove(removed);
            log.info("FavoritePair deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return favorites.size();
    }
}
