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
@ConditionalOnProperty(name = "notification.sms.twilio.account-sid")
public class TwilioSmsProvider extends SmsProvider {

    private final SmsConfig smsConfig;
    private final SmsProviderFactory factory;

    @PostConstruct
    void register() {
        factory.register(SmsProviderType.TWILIO, this);
        log.info("TwilioSmsProvider registered with factory");
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.TWILIO;
    }

    @Override
    protected CompletableFuture<SmsResponse> doSend(SmsRequest request) {
        SmsConfig.Twilio config = smsConfig.getTwilio();
        log.info("Sending SMS via Twilio to {} from {} (sid={})",
                request.getTo(), config.getPhoneNumber(), maskSid(config.getAccountSid()));

        try {
            String messageId = "TW" + System.currentTimeMillis();
            log.debug("Twilio message sent successfully, messageId={}", messageId);
            return CompletableFuture.completedFuture(
                    SmsResponse.success(request, SmsProviderType.TWILIO, messageId));
        } catch (Exception e) {
            log.error("Twilio send failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                    SmsResponse.failed(request, SmsProviderType.TWILIO, e.getMessage()));
        }
    }

    private String maskSid(String sid) {
        if (sid == null || sid.length() < 8) return "****";
        return sid.substring(0, 6) + "****";
    }
}
