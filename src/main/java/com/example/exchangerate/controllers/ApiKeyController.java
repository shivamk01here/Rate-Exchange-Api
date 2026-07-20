package com.example.exchangerate.controllers;

import com.example.exchangerate.apikey.ApiKey;
import com.example.exchangerate.apikey.ApiKeyService;
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
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiKey createApiKey(@Valid @RequestBody ApiKey apiKey) {
        if (apiKey.getLabel() == null || apiKey.getLabel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Label is required");
        }
        log.info("Creating API key: label={}", apiKey.getLabel());
        return apiKeyService.createApiKey(apiKey);
    }

    @GetMapping
    public List<ApiKey> getAllApiKeys() {
        return apiKeyService.getAllApiKeys();
    }

    @GetMapping("/{id}")
    public ApiKey getApiKey(@PathVariable String id) {
        return apiKeyService.getApiKey(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ApiKey not found: " + id));
    }

    @GetMapping("/validate")
    public Map<String, Object> validateApiKey(@RequestParam String key) {
        return apiKeyService.validateKey(key)
                .map(apiKey -> Map.<String, Object>of(
                        "valid", true,
                        "id", apiKey.getId(),
                        "label", apiKey.getLabel(),
                        "requestsPerMinute", apiKey.getRequestsPerMinute()))
                .orElse(Map.of("valid", false));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteApiKey(@PathVariable String id) {
        boolean deleted = apiKeyService.deleteApiKey(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ApiKey not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PatchMapping("/{id}/toggle")
    public ApiKey toggleApiKey(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", true);
        return apiKeyService.toggleApiKey(id, enabled);
    }

    @GetMapping("/count")
    public Map<String, Object> getApiKeyCount() {
        return Map.of("count", apiKeyService.getApiKeyCount());
    }
}
