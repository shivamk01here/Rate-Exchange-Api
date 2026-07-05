package com.example.exchangerate.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppProviderFactory factory;
    private final WhatsAppConfig whatsAppConfig;

    public CompletableFuture<WhatsAppResponse> send(WhatsAppRequest request) {
        if (!whatsAppConfig.isEnabled()) {
            log.warn("WhatsApp notifications disabled, skipping send to {}", request.getTo());
            return CompletableFuture.completedFuture(
                    WhatsAppResponse.failed(request, whatsAppConfig.getDefaultProvider(), "WhatsApp disabled"));
        }

        WhatsAppProvider provider = factory.getProvider(whatsAppConfig.getDefaultProvider());

        if (request.getFrom() == null) {
            request.setFrom(whatsAppConfig.getFrom());
        }

        log.debug("Delegating WhatsApp send to provider={}", provider.getProviderType());
        return provider.send(request);
    }

    public CompletableFuture<WhatsAppResponse> sendWithProvider(WhatsAppRequest request, WhatsAppProviderType providerType) {
        if (!whatsAppConfig.isEnabled()) {
            log.warn("WhatsApp notifications disabled, skipping send to {}", request.getTo());
            return CompletableFuture.completedFuture(
                    WhatsAppResponse.failed(request, providerType, "WhatsApp disabled"));
        }

        WhatsAppProvider provider = factory.getProvider(providerType);

        if (request.getFrom() == null) {
            request.setFrom(whatsAppConfig.getFrom());
        }

        log.debug("Delegating WhatsApp send to provider={}", provider.getProviderType());
        return provider.send(request);
    }
}
