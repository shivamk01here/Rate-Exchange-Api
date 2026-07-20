package com.example.exchangerate.apikey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ApiKeyRepository {

    private final ConcurrentHashMap<String, ApiKey> apiKeys = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<ApiKey> apiKeyList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public ApiKey save(ApiKey apiKey) {
        String id = apiKey.getId() != null ? apiKey.getId() : String.valueOf(idCounter.incrementAndGet());
        ApiKey stored = ApiKey.builder()
                .id(id)
                .key(apiKey.getKey())
                .label(apiKey.getLabel())
                .requestsPerMinute(apiKey.getRequestsPerMinute())
                .enabled(apiKey.isEnabled())
                .createdAt(apiKey.getCreatedAt() != null ? apiKey.getCreatedAt() : java.time.Instant.now())
                .lastUsedAt(apiKey.getLastUsedAt())
                .usageCount(apiKey.getUsageCount())
                .build();

        if (apiKeys.putIfAbsent(id, stored) == null) {
            apiKeyList.add(stored);
        } else {
            apiKeys.put(id, stored);
            for (int i = 0; i < apiKeyList.size(); i++) {
                if (id.equals(apiKeyList.get(i).getId())) {
                    apiKeyList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("ApiKey saved: id={} label={}", id, stored.getLabel());
        return stored;
    }

    public Optional<ApiKey> findById(String id) {
        return Optional.ofNullable(apiKeys.get(id));
    }

    public Optional<ApiKey> findByKey(String key) {
        return apiKeyList.stream()
                .filter(k -> key.equals(k.getKey()))
                .findFirst();
    }

    public List<ApiKey> findAll() {
        return new ArrayList<>(apiKeyList);
    }

    public boolean deleteById(String id) {
        ApiKey removed = apiKeys.remove(id);
        if (removed != null) {
            apiKeyList.remove(removed);
            log.info("ApiKey deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return apiKeys.size();
    }

    public void updateLastUsed(String id, java.time.Instant timestamp) {
        ApiKey existing = apiKeys.get(id);
        if (existing != null) {
            ApiKey updated = ApiKey.builder()
                    .id(existing.getId())
                    .key(existing.getKey())
                    .label(existing.getLabel())
                    .requestsPerMinute(existing.getRequestsPerMinute())
                    .enabled(existing.isEnabled())
                    .createdAt(existing.getCreatedAt())
                    .lastUsedAt(timestamp)
                    .usageCount(existing.getUsageCount() + 1)
                    .build();
            apiKeys.put(id, updated);
            for (int i = 0; i < apiKeyList.size(); i++) {
                if (id.equals(apiKeyList.get(i).getId())) {
                    apiKeyList.set(i, updated);
                    break;
                }
            }
        }
    }
}
