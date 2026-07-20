package com.example.exchangerate.apikey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKey createApiKey(ApiKey apiKey) {
        if (apiKey.getKey() == null || apiKey.getKey().isBlank()) {
            apiKey.setKey("ak-" + UUID.randomUUID().toString().replace("-", ""));
        }
        ApiKey saved = apiKeyRepository.save(apiKey);
        log.info("ApiKey created: id={} label={}", saved.getId(), saved.getLabel());
        return saved;
    }

    public Optional<ApiKey> getApiKey(String id) {
        return apiKeyRepository.findById(id);
    }

    public Optional<ApiKey> validateKey(String key) {
        return apiKeyRepository.findByKey(key)
                .filter(ApiKey::isEnabled);
    }

    public List<ApiKey> getAllApiKeys() {
        return apiKeyRepository.findAll();
    }

    public boolean deleteApiKey(String id) {
        boolean deleted = apiKeyRepository.deleteById(id);
        if (deleted) {
            log.info("ApiKey deleted: id={}", id);
        }
        return deleted;
    }

    public ApiKey toggleApiKey(String id, boolean enabled) {
        return apiKeyRepository.findById(id)
                .map(existing -> {
                    ApiKey updated = ApiKey.builder()
                            .id(existing.getId())
                            .key(existing.getKey())
                            .label(existing.getLabel())
                            .requestsPerMinute(existing.getRequestsPerMinute())
                            .enabled(enabled)
                            .createdAt(existing.getCreatedAt())
                            .lastUsedAt(existing.getLastUsedAt())
                            .usageCount(existing.getUsageCount())
                            .build();
                    ApiKey saved = apiKeyRepository.save(updated);
                    log.info("ApiKey {} toggled to enabled={}", id, enabled);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("ApiKey not found: " + id));
    }

    public long getApiKeyCount() {
        return apiKeyRepository.count();
    }
}
