package com.example.exchangerate.apikey;

import com.example.exchangerate.controllers.ApiKeyController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyServiceTest {

    private ApiKeyService apiKeyService;
    private ApiKeyRepository apiKeyRepository;

    @BeforeEach
    void setUp() {
        apiKeyRepository = new ApiKeyRepository();
        apiKeyService = new ApiKeyService(apiKeyRepository);
    }

    @Test
    void createApiKey_returnsSavedApiKeyWithId() {
        ApiKey apiKey = ApiKey.builder()
                .key("ak-testkey123")
                .label("Test Key")
                .requestsPerMinute(50)
                .enabled(true)
                .build();

        ApiKey saved = apiKeyService.createApiKey(apiKey);

        assertNotNull(saved.getId());
        assertEquals("ak-testkey123", saved.getKey());
        assertEquals("Test Key", saved.getLabel());
        assertTrue(saved.isEnabled());
    }

    @Test
    void createApiKey_generatesKeyWhenNotProvided() {
        ApiKey apiKey = ApiKey.builder()
                .label("Auto Key")
                .enabled(true)
                .build();

        ApiKey saved = apiKeyService.createApiKey(apiKey);

        assertNotNull(saved.getKey());
        assertTrue(saved.getKey().startsWith("ak-"));
    }

    @Test
    void getApiKey_returnsApiKeyWhenExists() {
        ApiKey saved = apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-existing").label("Existing").enabled(true).build());

        ApiKey found = apiKeyService.getApiKey(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getApiKey_returnsEmptyWhenNotFound() {
        assertTrue(apiKeyService.getApiKey("nonexistent").isEmpty());
    }

    @Test
    void validateKey_returnsApiKeyWhenValidAndEnabled() {
        ApiKey saved = apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-valid").label("Valid").enabled(true).build());

        ApiKey validated = apiKeyService.validateKey("ak-valid").orElse(null);

        assertNotNull(validated);
        assertEquals(saved.getId(), validated.getId());
    }

    @Test
    void validateKey_returnsEmptyWhenDisabled() {
        apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-disabled").label("Disabled").enabled(false).build());

        assertTrue(apiKeyService.validateKey("ak-disabled").isEmpty());
    }

    @Test
    void validateKey_returnsEmptyWhenNotFound() {
        assertTrue(apiKeyService.validateKey("ak-nonexistent").isEmpty());
    }

    @Test
    void getAllApiKeys_returnsAllCreatedKeys() {
        apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-key1").label("Key 1").enabled(true).build());
        apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-key2").label("Key 2").enabled(false).build());

        List<ApiKey> all = apiKeyService.getAllApiKeys();

        assertEquals(2, all.size());
    }

    @Test
    void deleteApiKey_removesApiKey() {
        ApiKey saved = apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-delete").label("Delete").enabled(true).build());

        assertTrue(apiKeyService.deleteApiKey(saved.getId()));
        assertTrue(apiKeyService.getApiKey(saved.getId()).isEmpty());
    }

    @Test
    void deleteApiKey_returnsFalseForNonexistent() {
        assertFalse(apiKeyService.deleteApiKey("nonexistent"));
    }

    @Test
    void toggleApiKey_enablesAndDisables() {
        ApiKey saved = apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-toggle").label("Toggle").enabled(false).build());

        ApiKey toggledOn = apiKeyService.toggleApiKey(saved.getId(), true);
        assertTrue(toggledOn.isEnabled());

        ApiKey toggledOff = apiKeyService.toggleApiKey(saved.getId(), false);
        assertFalse(toggledOff.isEnabled());
    }

    @Test
    void toggleApiKey_throwsForNonexistent() {
        assertThrows(IllegalArgumentException.class,
                () -> apiKeyService.toggleApiKey("nonexistent", true));
    }

    @Test
    void getApiKeyCount_returnsCorrectCount() {
        assertEquals(0, apiKeyService.getApiKeyCount());

        apiKeyService.createApiKey(ApiKey.builder()
                .key("ak-one").label("One").enabled(true).build());

        assertEquals(1, apiKeyService.getApiKeyCount());
    }
}
