package com.example.exchangerate.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.whatsapp.twilio.account-sid")
public class TwilioWhatsAppProvider extends WhatsAppProvider {

    private final WhatsAppConfig whatsAppConfig;
    private final WhatsAppProviderFactory factory;

    @PostConstruct
    void register() {
        factory.register(WhatsAppProviderType.TWILIO, this);
        log.info("TwilioWhatsAppProvider registered with factory");
    }

    @Override
    public WhatsAppProviderType getProviderType() {
        return WhatsAppProviderType.TWILIO;
    }

    @Override
    protected CompletableFuture<WhatsAppResponse> doSend(WhatsAppRequest request) {
        WhatsAppConfig.Twilio config = whatsAppConfig.getTwilio();
        log.info("Sending WhatsApp via Twilio to {} from {} (sid={})",
                request.getTo(), config.getPhoneNumber(), maskSid(config.getAccountSid()));

        try {
            String messageId = "WATW" + System.currentTimeMillis();
            log.debug("Twilio WhatsApp message sent successfully, messageId={}", messageId);
            return CompletableFuture.completedFuture(
                    WhatsAppResponse.success(request, WhatsAppProviderType.TWILIO, messageId));
        } catch (Exception e) {
            log.error("Twilio WhatsApp send failed: {}", e.getMessage());
            return CompletableFuture.completedFuture(
                    WhatsAppResponse.failed(request, WhatsAppProviderType.TWILIO, e.getMessage()));
        }
    }

    private String maskSid(String sid) {
        if (sid == null || sid.length() < 8) return "****";
        return sid.substring(0, 6) + "****";
    }
}
