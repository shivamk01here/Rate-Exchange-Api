package com.example.exchangerate.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class SmsProviderFactory implements SmsProvider.Factory {

    private final ConcurrentMap<SmsProviderType, SmsProvider> providers = new ConcurrentHashMap<>();

    @Override
    public void register(SmsProviderType type, SmsProvider provider) {
        log.info("Registering SMS provider type={} implementation={}", type, provider.getClass().getSimpleName());
        SmsProvider existing = providers.putIfAbsent(type, provider);
        if (existing != null) {
            throw new IllegalStateException("SMS provider already registered for type: " + type);
        }
    }

    @Override
    public SmsProvider getProvider(SmsProviderType type) {
        return Optional.ofNullable(providers.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No SMS provider registered for: " + type));
    }
}
