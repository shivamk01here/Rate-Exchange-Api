package com.example.exchangerate.controllers;

import com.example.exchangerate.apikey.ApiKey;
import com.example.exchangerate.apikey.ApiKeyRepository;
import com.example.exchangerate.apikey.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyControllerTest {

    private ApiKeyController controller;

    @BeforeEach
    void setUp() {
        ApiKeyRepository repository = new ApiKeyRepository();
        ApiKeyService service = new ApiKeyService(repository);
        controller = new ApiKeyController(service);
    }

    @Test
    void createApiKey_returnsCreatedApiKey() {
        ApiKey apiKey = ApiKey.builder()
                .key("ak-test123")
                .label("Test Key")
                .enabled(true)
                .build();

        ApiKey result = controller.createApiKey(apiKey);

        assertNotNull(result.getId());
        assertEquals("Test Key", result.getLabel());
    }

    @Test
    void createApiKey_throwsWhenLabelMissing() {
        ApiKey apiKey = ApiKey.builder()
                .key("ak-test")
                .enabled(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createApiKey(apiKey));
    }

    @Test
    void getAllApiKeys_returnsAllKeys() {
        controller.createApiKey(ApiKey.builder()
                .key("ak-1").label("Key 1").enabled(true).build());
        controller.createApiKey(ApiKey.builder()
                .key("ak-2").label("Key 2").enabled(false).build());

        List<ApiKey> all = controller.getAllApiKeys();

        assertEquals(2, all.size());
    }

    @Test
    void getApiKey_returnsKeyById() {
        ApiKey created = controller.createApiKey(ApiKey.builder()
                .key("ak-find").label("Find Me").enabled(true).build());

        ApiKey result = controller.getApiKey(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getApiKey_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getApiKey("bad-id"));
    }

    @Test
    void deleteApiKey_returnsSuccess() {
        ApiKey created = controller.createApiKey(ApiKey.builder()
                .key("ak-del").label("Delete Me").enabled(true).build());

        Map<String, String> result = controller.deleteApiKey(created.getId());

        assertEquals("deleted", result.get("status"));
        assertThrows(ResponseStatusException.class, () -> controller.getApiKey(created.getId()));
    }

    @Test
    void deleteApiKey_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.deleteApiKey("bad-id"));
    }

    @Test
    void toggleApiKey_changesEnabledState() {
        ApiKey created = controller.createApiKey(ApiKey.builder()
                .key("ak-tog").label("Toggle").enabled(false).build());

        ApiKey toggled = controller.toggleApiKey(created.getId(), Map.of("enabled", true));

        assertTrue(toggled.isEnabled());
    }

    @Test
    void validateApiKey_returnsValidForExistingKey() {
        controller.createApiKey(ApiKey.builder()
                .key("ak-valid").label("Valid").enabled(true).build());

        Map<String, Object> result = controller.validateApiKey("ak-valid");

        assertEquals(true, result.get("valid"));
        assertEquals("Valid", result.get("label"));
    }

    @Test
    void validateApiKey_returnsInvalidForMissingKey() {
        Map<String, Object> result = controller.validateApiKey("ak-nonexistent");

        assertEquals(false, result.get("valid"));
    }

    @Test
    void getApiKeyCount_returnsCount() {
        controller.createApiKey(ApiKey.builder()
                .key("ak-count").label("Count").enabled(true).build());

        Map<String, Object> result = controller.getApiKeyCount();

        assertEquals(1L, result.get("count"));
    }
}
