package com.example.exchangerate.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsProviderFactory factory;
    private final SmsConfig smsConfig;

    public CompletableFuture<SmsResponse> send(SmsRequest request) {
        if (!smsConfig.isEnabled()) {
            log.warn("SMS notifications disabled, skipping send to {}", request.getTo());
            return CompletableFuture.completedFuture(
                    SmsResponse.failed(request, smsConfig.getDefaultProvider(), "SMS disabled"));
        }

        SmsProvider provider = factory.getProvider(smsConfig.getDefaultProvider());

        if (request.getFrom() == null) {
            request.setFrom(smsConfig.getFrom());
        }

        log.debug("Delegating SMS send to provider={}", provider.getProviderType());
        return provider.send(request);
    }

    public CompletableFuture<SmsResponse> sendWithProvider(SmsRequest request, SmsProviderType providerType) {
        if (!smsConfig.isEnabled()) {
            log.warn("SMS notifications disabled, skipping send to {}", request.getTo());
            return CompletableFuture.completedFuture(
                    SmsResponse.failed(request, providerType, "SMS disabled"));
        }

        SmsProvider provider = factory.getProvider(providerType);

        if (request.getFrom() == null) {
            request.setFrom(smsConfig.getFrom());
        }

        log.debug("Delegating SMS send to provider={}", provider.getProviderType());
        return provider.send(request);
    }
}
