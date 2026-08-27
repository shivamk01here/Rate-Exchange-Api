package com.example.exchangerate.controllers;

import com.example.exchangerate.ratebookmark.RateBookmark;
import com.example.exchangerate.ratebookmark.RateBookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rate-bookmarks")
@RequiredArgsConstructor
public class RateBookmarkController {

    private final RateBookmarkService rateBookmarkService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public RateBookmark createBookmark(@Valid @RequestBody RateBookmark bookmark) {
        log.info("Creating rate bookmark: {}->{}", bookmark.getFromCurrency(), bookmark.getToCurrency());
        return rateBookmarkService.createBookmark(bookmark);
    }

    @GetMapping
    public List<RateBookmark> getAllBookmarks() {
        return rateBookmarkService.getAllBookmarks();
    }

    @GetMapping("/{id}")
    public RateBookmark getBookmark(@PathVariable String id) {
        return rateBookmarkService.getBookmark(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate bookmark not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<RateBookmark> getBookmarksByPair(@RequestParam String from, @RequestParam String to) {
        return rateBookmarkService.getBookmarksByPair(from, to);
    }

    @GetMapping("/by-provider")
    public List<RateBookmark> getBookmarksByProvider(@RequestParam String providerCode) {
        return rateBookmarkService.getBookmarksByProvider(providerCode);
    }

    @PutMapping("/{id}")
    public RateBookmark updateBookmark(@PathVariable String id, @Valid @RequestBody RateBookmark bookmark) {
        try {
            return rateBookmarkService.updateBookmark(id, bookmark);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteBookmark(@PathVariable String id) {
        boolean deleted = rateBookmarkService.deleteBookmark(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate bookmark not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @GetMapping("/count")
    public Map<String, Object> getBookmarkCount() {
        return Map.of("count", rateBookmarkService.getBookmarkCount());
    }
}
