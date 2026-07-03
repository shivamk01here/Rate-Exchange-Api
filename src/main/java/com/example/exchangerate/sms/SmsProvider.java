package com.example.exchangerate.sms;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class SmsProvider {

    public abstract SmsProviderType getProviderType();

    protected abstract CompletableFuture<SmsResponse> doSend(SmsRequest request);

    public final CompletableFuture<SmsResponse> send(SmsRequest request) {
        log.info("SMS provider {} sending to {}", getProviderType(), maskPhone(request.getTo()));
        return doSend(request)
                .exceptionally(throwable -> {
                    log.error("SMS provider {} failed: {}", getProviderType(), throwable.getMessage());
                    return SmsResponse.failed(request, getProviderType(), throwable.getMessage());
                });
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    public interface Factory {
        void register(SmsProviderType type, SmsProvider provider);

        SmsProvider getProvider(SmsProviderType type);
    }
}
