package com.example.exchangerate.bookmark;

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
public class ConversionBookmarkRepository {

    private final ConcurrentHashMap<String, ConversionBookmark> bookmarks = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<ConversionBookmark> bookmarkList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public ConversionBookmark save(ConversionBookmark bookmark) {
        String id = bookmark.getId() != null ? bookmark.getId() : String.valueOf(idCounter.incrementAndGet());
        ConversionBookmark stored = ConversionBookmark.builder()
                .id(id)
                .name(bookmark.getName())
                .fromCurrency(bookmark.getFromCurrency())
                .toCurrency(bookmark.getToCurrency())
                .amount(bookmark.getAmount())
                .createdAt(bookmark.getCreatedAt() != null ? bookmark.getCreatedAt() : java.time.Instant.now())
                .lastUsedAt(bookmark.getLastUsedAt())
                .useCount(bookmark.getUseCount())
                .build();

        if (bookmarks.putIfAbsent(id, stored) == null) {
            bookmarkList.add(stored);
        } else {
            bookmarks.put(id, stored);
            for (int i = 0; i < bookmarkList.size(); i++) {
                if (id.equals(bookmarkList.get(i).getId())) {
                    bookmarkList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("ConversionBookmark saved: id={} name={} {}->{}",
                id, stored.getName(), stored.getFromCurrency(), stored.getToCurrency());
        return stored;
    }

    public Optional<ConversionBookmark> findById(String id) {
        return Optional.ofNullable(bookmarks.get(id));
    }

    public List<ConversionBookmark> findAll() {
        return new ArrayList<>(bookmarkList);
    }

    public List<ConversionBookmark> findByCurrencyPair(String from, String to) {
        return bookmarkList.stream()
                .filter(b -> from.equalsIgnoreCase(b.getFromCurrency())
                        && to.equalsIgnoreCase(b.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<ConversionBookmark> findByName(String name) {
        return bookmarkList.stream()
                .filter(b -> name.equalsIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        ConversionBookmark removed = bookmarks.remove(id);
        if (removed != null) {
            bookmarkList.remove(removed);
            log.info("ConversionBookmark deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return bookmarks.size();
    }

    public void recordUse(String id, java.time.Instant timestamp) {
        ConversionBookmark existing = bookmarks.get(id);
        if (existing != null) {
            ConversionBookmark updated = ConversionBookmark.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .fromCurrency(existing.getFromCurrency())
                    .toCurrency(existing.getToCurrency())
                    .amount(existing.getAmount())
                    .createdAt(existing.getCreatedAt())
                    .lastUsedAt(timestamp)
                    .useCount(existing.getUseCount() + 1)
                    .build();
            bookmarks.put(id, updated);
            for (int i = 0; i < bookmarkList.size(); i++) {
                if (id.equals(bookmarkList.get(i).getId())) {
                    bookmarkList.set(i, updated);
                    break;
                }
            }
        }
    }
}
