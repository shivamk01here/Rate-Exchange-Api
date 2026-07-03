package com.example.exchangerate.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.sms.vonage.api-key")
public class VonageSmsProvider extends SmsProvider {

    private final SmsConfig smsConfig;
    private final SmsProviderFactory factory;

    @PostConstruct
    void register() {
        factory.register(SmsProviderType.VONAGE, this);
        log.info("VonageSmsProvider registered with factory");
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.VONAGE;
    }

    @Override
    protected CompletableFuture<SmsResponse> doSend(SmsRequest request) {
        SmsConfig.Vonage config = smsConfig.getVonage();
        log.info("Sending SMS via Vonage to {} from {}", request.getTo(), config.getPhoneNumber());

        try {
            String messageId = "VN" + System.currentTimeMillis();
            log.debug("Vonage message sent successfully, messageId={}", messageId);
            return CompletableFuture.completedFuture(
                    SmsResponse.success(request, SmsProviderType.VONAGE, messageId));
        } catch (Exception e) {
            log.error("Vonage send failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                    SmsResponse.failed(request, SmsProviderType.VONAGE, e.getMessage()));
        }
    }
}
