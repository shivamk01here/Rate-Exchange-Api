package com.example.exchangerate.whatsapp;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class WhatsAppProvider {

    public abstract WhatsAppProviderType getProviderType();

    protected abstract CompletableFuture<WhatsAppResponse> doSend(WhatsAppRequest request);

    public final CompletableFuture<WhatsAppResponse> send(WhatsAppRequest request) {
        log.info("WhatsApp provider {} sending to {}", getProviderType(), maskPhone(request.getTo()));
        return doSend(request)
                .exceptionally(throwable -> {
                    log.error("WhatsApp provider {} failed: {}", getProviderType(), throwable.getMessage());
                    return WhatsAppResponse.failed(request, getProviderType(), throwable.getMessage());
                });
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    public interface Factory {
        void register(WhatsAppProviderType type, WhatsAppProvider provider);

        WhatsAppProvider getProvider(WhatsAppProviderType type);
    }
}
