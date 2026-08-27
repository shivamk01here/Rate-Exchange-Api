package com.example.exchangerate.ratebookmark;

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
public class RateBookmarkRepository {

    private final ConcurrentHashMap<String, RateBookmark> bookmarks = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<RateBookmark> bookmarkList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public RateBookmark save(RateBookmark bookmark) {
        String id = bookmark.getId() != null ? bookmark.getId() : String.valueOf(idCounter.incrementAndGet());
        RateBookmark stored = RateBookmark.builder()
                .id(id)
                .fromCurrency(bookmark.getFromCurrency())
                .toCurrency(bookmark.getToCurrency())
                .rate(bookmark.getRate())
                .label(bookmark.getLabel())
                .providerCode(bookmark.getProviderCode())
                .bookmarkedAt(bookmark.getBookmarkedAt() != null ? bookmark.getBookmarkedAt() : java.time.Instant.now())
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

        log.debug("RateBookmark saved: id={} {}->{} rate={}", id, stored.getFromCurrency(), stored.getToCurrency(), stored.getRate());
        return stored;
    }

    public Optional<RateBookmark> findById(String id) {
        return Optional.ofNullable(bookmarks.get(id));
    }

    public List<RateBookmark> findAll() {
        return new ArrayList<>(bookmarkList);
    }

    public List<RateBookmark> findByCurrencyPair(String from, String to) {
        return bookmarkList.stream()
                .filter(b -> from.equalsIgnoreCase(b.getFromCurrency())
                        && to.equalsIgnoreCase(b.getToCurrency()))
                .collect(Collectors.toList());
    }

    public List<RateBookmark> findByProvider(String providerCode) {
        return bookmarkList.stream()
                .filter(b -> providerCode != null && providerCode.equalsIgnoreCase(b.getProviderCode()))
                .collect(Collectors.toList());
    }

    public boolean deleteById(String id) {
        RateBookmark removed = bookmarks.remove(id);
        if (removed != null) {
            bookmarkList.remove(removed);
            log.info("RateBookmark deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return bookmarks.size();
    }
}
