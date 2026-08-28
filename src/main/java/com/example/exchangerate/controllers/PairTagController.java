package com.example.exchangerate.controllers;

import com.example.exchangerate.pairtag.PairTag;
import com.example.exchangerate.pairtag.PairTagService;
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
@RequestMapping("/api/pair-tags")
@RequiredArgsConstructor
public class PairTagController {

    private final PairTagService pairTagService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public PairTag createTag(@Valid @RequestBody PairTag tag) {
        log.info("Creating pair tag: {}->{} tag={}", tag.getFromCurrency(), tag.getToCurrency(), tag.getTag());
        return pairTagService.createTag(tag);
    }

    @GetMapping
    public List<PairTag> getAllTags() {
        return pairTagService.getAllTags();
    }

    @GetMapping("/{id}")
    public PairTag getTag(@PathVariable String id) {
        return pairTagService.getTag(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pair tag not found: " + id));
    }

    @GetMapping("/by-pair")
    public List<PairTag> getTagsByPair(@RequestParam String from, @RequestParam String to) {
        return pairTagService.getTagsByPair(from, to);
    }

    @GetMapping("/by-tag")
    public List<PairTag> getTagsByTag(@RequestParam String tag) {
        return pairTagService.getTagsByTag(tag);
    }

    @GetMapping("/distinct")
    public List<String> getDistinctTags() {
        return pairTagService.getDistinctTags();
    }

    @PutMapping("/{id}")
    public PairTag updateTag(@PathVariable String id, @Valid @RequestBody PairTag tag) {
        try {
            return pairTagService.updateTag(id, tag);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteTag(@PathVariable String id) {
        boolean deleted = pairTagService.deleteTag(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pair tag not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @GetMapping("/count")
    public Map<String, Object> getTagCount() {
        return Map.of("count", pairTagService.getTagCount());
    }
}
