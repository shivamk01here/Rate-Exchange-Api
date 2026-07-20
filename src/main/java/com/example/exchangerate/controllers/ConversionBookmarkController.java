package com.example.exchangerate.controllers;

import com.example.exchangerate.bookmark.ConversionBookmark;
import com.example.exchangerate.bookmark.ConversionBookmarkService;
import com.example.exchangerate.models.ExchangeRateResponse;
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
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class ConversionBookmarkController {

    private final ConversionBookmarkService bookmarkService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConversionBookmark createBookmark(@Valid @RequestBody ConversionBookmark bookmark) {
        if (bookmark.getName() == null || bookmark.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        log.info("Creating bookmark: name={} {}->{}", bookmark.getName(), bookmark.getFromCurrency(), bookmark.getToCurrency());
        return bookmarkService.createBookmark(bookmark);
    }

    @GetMapping
    public List<ConversionBookmark> getAllBookmarks() {
        return bookmarkService.getAllBookmarks();
    }

    @GetMapping("/{id}")
    public ConversionBookmark getBookmark(@PathVariable String id) {
        return bookmarkService.getBookmark(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bookmark not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<ConversionBookmark> getBookmarksByPair(@RequestParam String from, @RequestParam String to) {
        return bookmarkService.getBookmarksByCurrencyPair(from, to);
    }

    @GetMapping("/by-name")
    public List<ConversionBookmark> getBookmarksByName(@RequestParam String name) {
        return bookmarkService.getBookmarksByName(name);
    }

    @PutMapping("/{id}")
    public ConversionBookmark updateBookmark(@PathVariable String id, @Valid @RequestBody ConversionBookmark bookmark) {
        try {
            return bookmarkService.updateBookmark(id, bookmark);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteBookmark(@PathVariable String id) {
        boolean deleted = bookmarkService.deleteBookmark(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bookmark not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PostMapping("/{id}/execute")
    public CompletableFuture<ExchangeRateResponse> executeBookmark(@PathVariable String id) {
        log.info("Executing bookmark: id={}", id);
        try {
            return bookmarkService.executeBookmark(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/count")
    public Map<String, Object> getBookmarkCount() {
        return Map.of("count", bookmarkService.getBookmarkCount());
    }
}
