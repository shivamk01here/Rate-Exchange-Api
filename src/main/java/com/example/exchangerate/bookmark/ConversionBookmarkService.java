package com.example.exchangerate.bookmark;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionBookmarkService {

    private final ConversionBookmarkRepository bookmarkRepository;
    private final ExchangeRateOrchestrationService orchestrationService;

    public ConversionBookmark createBookmark(ConversionBookmark bookmark) {
        ConversionBookmark saved = bookmarkRepository.save(bookmark);
        log.info("Bookmark created: id={} name={} {}->{} amount={}",
                saved.getId(), saved.getName(), saved.getFromCurrency(),
                saved.getToCurrency(), saved.getAmount());
        return saved;
    }

    public Optional<ConversionBookmark> getBookmark(String id) {
        return bookmarkRepository.findById(id);
    }

    public List<ConversionBookmark> getAllBookmarks() {
        return bookmarkRepository.findAll();
    }

    public List<ConversionBookmark> getBookmarksByCurrencyPair(String from, String to) {
        return bookmarkRepository.findByCurrencyPair(from, to);
    }

    public List<ConversionBookmark> getBookmarksByName(String name) {
        return bookmarkRepository.findByName(name);
    }

    public boolean deleteBookmark(String id) {
        boolean deleted = bookmarkRepository.deleteById(id);
        if (deleted) {
            log.info("Bookmark deleted: id={}", id);
        }
        return deleted;
    }

    public ConversionBookmark updateBookmark(String id, ConversionBookmark updated) {
        return bookmarkRepository.findById(id)
                .map(existing -> {
                    ConversionBookmark merged = ConversionBookmark.builder()
                            .id(existing.getId())
                            .name(updated.getName() != null ? updated.getName() : existing.getName())
                            .fromCurrency(updated.getFromCurrency() != null ? updated.getFromCurrency() : existing.getFromCurrency())
                            .toCurrency(updated.getToCurrency() != null ? updated.getToCurrency() : existing.getToCurrency())
                            .amount(updated.getAmount() != null ? updated.getAmount() : existing.getAmount())
                            .createdAt(existing.getCreatedAt())
                            .lastUsedAt(existing.getLastUsedAt())
                            .useCount(existing.getUseCount())
                            .build();
                    ConversionBookmark saved = bookmarkRepository.save(merged);
                    log.info("Bookmark updated: id={} name={}", id, saved.getName());
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found: " + id));
    }

    public long getBookmarkCount() {
        return bookmarkRepository.count();
    }

    public CompletableFuture<ExchangeRateResponse> executeBookmark(String id) {
        return bookmarkRepository.findById(id)
                .map(bookmark -> {
                    bookmarkRepository.recordUse(id, Instant.now());
                    ExchangeRateRequest request = ExchangeRateRequest.builder()
                            .fromCurrency(bookmark.getFromCurrency())
                            .toCurrency(bookmark.getToCurrency())
                            .amount(bookmark.getAmount() != null ? bookmark.getAmount() : BigDecimal.ONE)
                            .build();
                    log.info("Executing bookmark: id={} name={} {}->{} amount={}",
                            id, bookmark.getName(), bookmark.getFromCurrency(),
                            bookmark.getToCurrency(), request.getAmount());
                    return orchestrationService.getRate(request);
                })
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found: " + id));
    }
}
