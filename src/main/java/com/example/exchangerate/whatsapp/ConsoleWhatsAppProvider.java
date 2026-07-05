package com.example.exchangerate.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsoleWhatsAppProvider extends WhatsAppProvider {

    private final WhatsAppProviderFactory factory;

    @PostConstruct
    void register() {
        factory.register(WhatsAppProviderType.CONSOLE, this);
        log.info("ConsoleWhatsAppProvider registered with factory");
    }

    @Override
    public WhatsAppProviderType getProviderType() {
        return WhatsAppProviderType.CONSOLE;
    }

    @Override
    protected CompletableFuture<WhatsAppResponse> doSend(WhatsAppRequest request) {
        log.info("========================================");
        log.info("CONSOLE WhatsApp (simulated send)");
        log.info("  To:      {}", request.getTo());
        log.info("  From:    {}", request.getFrom());
        log.info("  Message: {}", request.getMessage());
        log.info("========================================");

        String messageId = "WACON" + System.currentTimeMillis();
        return CompletableFuture.completedFuture(
                WhatsAppResponse.success(request, WhatsAppProviderType.CONSOLE, messageId));
    }
}
