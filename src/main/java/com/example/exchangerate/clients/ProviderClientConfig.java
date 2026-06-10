package com.example.exchangerate.clients;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Map;

@Slf4j
@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class ProviderClientConfig {

    public static final String BASE_URL = "baseUrl";
    public static final int CONNECT_TIMEOUT_MILLIS = 5000;

    String baseUrl;

    public static <T extends ProviderClientConfig> void populateFromMap(Map<String, String> configs,
            T target) {
        try {
            BeanUtils.populate(target, configs);
        } catch (Exception e) {
            log.error("Failed to populate config from map", e);
            throw new RuntimeException("Config population failed", e);
        }
    }

    public static <T extends ProviderClientConfig> T fromStaticConfigs(
            Map<String, String> staticConfigs,
            Map<String, String> dynamicConfigs,
            T instance) {
        instance.setBaseUrl(staticConfigs.get(BASE_URL));
        populateFromMap(dynamicConfigs, instance);
        return instance;
    }
}
