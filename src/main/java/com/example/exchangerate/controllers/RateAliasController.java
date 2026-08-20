package com.example.exchangerate.controllers;

import com.example.exchangerate.aliases.RateAlias;
import com.example.exchangerate.aliases.RateAliasService;
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
@RequestMapping("/api/aliases")
@RequiredArgsConstructor
public class RateAliasController {

    private final RateAliasService aliasService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public RateAlias createAlias(@Valid @RequestBody RateAlias alias) {
        if (alias.getAlias() == null || alias.getAlias().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alias name is required");
        }
        log.info("Creating alias: alias={} {}->{}", alias.getAlias(), alias.getFromCurrency(), alias.getToCurrency());
        return aliasService.createAlias(alias);
    }

    @GetMapping
    public List<RateAlias> getAllAliases() {
        return aliasService.getAllAliases();
    }

    @GetMapping("/{id}")
    public RateAlias getAlias(@PathVariable String id) {
        return aliasService.getAlias(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alias not found: " + id));
    }

    @GetMapping("/lookup")
    public RateAlias lookupByAlias(@RequestParam String name) {
        return aliasService.lookupByAlias(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No alias found for: " + name));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteAlias(@PathVariable String id) {
        boolean deleted = aliasService.deleteAlias(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alias not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @GetMapping("/count")
    public Map<String, Object> getAliasCount() {
        return Map.of("count", aliasService.getAliasCount());
    }
}
