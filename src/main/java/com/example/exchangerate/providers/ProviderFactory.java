package com.example.exchangerate.providers;

import com.example.exchangerate.clients.ProviderClientConfig;
import com.example.exchangerate.models.ProviderCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ProviderFactory implements ExchangeRateProvider.Factory {

    private final ConcurrentMap<ProviderCodes, ExchangeRateProvider> providers = new ConcurrentHashMap<>();
    private final ConcurrentMap<ProviderCodes, Class<? extends ProviderClientConfig>> configClasses = new ConcurrentHashMap<>();

    @Override
    public void register(ProviderCodes code, ExchangeRateProvider provider,
            Class<? extends ProviderClientConfig> configClass) {
        log.info("Registering provider={} with connector={} config={}",
                code, provider.getClass().getSimpleName(), configClass.getSimpleName());

        ExchangeRateProvider existing = providers.putIfAbsent(code, provider);
        if (existing != null) {
            throw new IllegalStateException("Provider already registered for code: " + code);
        }

        configClasses.putIfAbsent(code, configClass);
    }

    @Override
    public ExchangeRateProvider getProvider(ProviderCodes code) {
        return Optional.ofNullable(providers.get(code))
                .orElseThrow(() -> new IllegalArgumentException("No provider registered for: " + code));
    }

    @Override
    public Class<? extends ProviderClientConfig> getConfigClass(ProviderCodes code) {
        return Optional.ofNullable(configClasses.get(code))
                .orElseThrow(() -> new IllegalArgumentException("No config class registered for: " + code));
    }
}
