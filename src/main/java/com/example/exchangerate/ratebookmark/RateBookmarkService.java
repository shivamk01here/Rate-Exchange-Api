package com.example.exchangerate.ratebookmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateBookmarkService {

    private final RateBookmarkRepository rateBookmarkRepository;

    public RateBookmark createBookmark(RateBookmark bookmark) {
        RateBookmark saved = rateBookmarkRepository.save(bookmark);
        log.info("Rate bookmark created: id={} {}->{} rate={}", saved.getId(), saved.getFromCurrency(), saved.getToCurrency(), saved.getRate());
        return saved;
    }

    public Optional<RateBookmark> getBookmark(String id) {
        return rateBookmarkRepository.findById(id);
    }

    public List<RateBookmark> getAllBookmarks() {
        return rateBookmarkRepository.findAll();
    }

    public List<RateBookmark> getBookmarksByPair(String from, String to) {
        return rateBookmarkRepository.findByCurrencyPair(from, to);
    }

    public List<RateBookmark> getBookmarksByProvider(String providerCode) {
        return rateBookmarkRepository.findByProvider(providerCode);
    }

    public boolean deleteBookmark(String id) {
        boolean deleted = rateBookmarkRepository.deleteById(id);
        if (deleted) {
            log.info("Rate bookmark deleted: id={}", id);
        }
        return deleted;
    }

    public RateBookmark updateBookmark(String id, RateBookmark updated) {
        return rateBookmarkRepository.findById(id)
                .map(existing -> {
                    RateBookmark merged = RateBookmark.builder()
                            .id(existing.getId())
                            .fromCurrency(updated.getFromCurrency() != null ? updated.getFromCurrency() : existing.getFromCurrency())
                            .toCurrency(updated.getToCurrency() != null ? updated.getToCurrency() : existing.getToCurrency())
                            .rate(updated.getRate() != null ? updated.getRate() : existing.getRate())
                            .label(updated.getLabel() != null ? updated.getLabel() : existing.getLabel())
                            .providerCode(updated.getProviderCode() != null ? updated.getProviderCode() : existing.getProviderCode())
                            .bookmarkedAt(existing.getBookmarkedAt())
                            .build();
                    RateBookmark saved = rateBookmarkRepository.save(merged);
                    log.info("Rate bookmark updated: id={}", id);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Rate bookmark not found: " + id));
    }

    public long getBookmarkCount() {
        return rateBookmarkRepository.count();
    }
}
