package com.example.exchangerate.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsoleSmsProvider extends SmsProvider {

    private final SmsProviderFactory factory;

    @PostConstruct
    void register() {
        factory.register(SmsProviderType.CONSOLE, this);
        log.info("ConsoleSmsProvider registered with factory");
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.CONSOLE;
    }

    @Override
    protected CompletableFuture<SmsResponse> doSend(SmsRequest request) {
        log.info("========================================");
        log.info("CONSOLE SMS (simulated send)");
        log.info("  To:      {}", request.getTo());
        log.info("  From:    {}", request.getFrom());
        log.info("  Message: {}", request.getMessage());
        log.info("========================================");

        String messageId = "CON" + System.currentTimeMillis();
        return CompletableFuture.completedFuture(
                SmsResponse.success(request, SmsProviderType.CONSOLE, messageId));
    }
}
