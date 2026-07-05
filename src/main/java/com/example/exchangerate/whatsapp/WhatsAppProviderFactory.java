package com.example.exchangerate.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class WhatsAppProviderFactory implements WhatsAppProvider.Factory {

    private final ConcurrentMap<WhatsAppProviderType, WhatsAppProvider> providers = new ConcurrentHashMap<>();

    @Override
    public void register(WhatsAppProviderType type, WhatsAppProvider provider) {
        log.info("Registering WhatsApp provider type={} implementation={}", type, provider.getClass().getSimpleName());
        WhatsAppProvider existing = providers.putIfAbsent(type, provider);
        if (existing != null) {
            throw new IllegalStateException("WhatsApp provider already registered for type: " + type);
        }
    }

    @Override
    public WhatsAppProvider getProvider(WhatsAppProviderType type) {
        return Optional.ofNullable(providers.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No WhatsApp provider registered for: " + type));
    }
}
