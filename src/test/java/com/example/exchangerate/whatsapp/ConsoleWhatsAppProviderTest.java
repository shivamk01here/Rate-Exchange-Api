package com.example.exchangerate.whatsapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleWhatsAppProviderTest {

    private ConsoleWhatsAppProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ConsoleWhatsAppProvider(new WhatsAppProviderFactory());
    }

    @Test
    void shouldReturnConsoleType() {
        assertThat(provider.getProviderType()).isEqualTo(WhatsAppProviderType.CONSOLE);
    }

    @Test
    void shouldSendSuccessfully() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test message")
                .build();

        CompletableFuture<WhatsAppResponse> future = provider.send(request);
        WhatsAppResponse response = future.join();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessageId()).startsWith("WACON");
        assertThat(response.getProvider()).isEqualTo(WhatsAppProviderType.CONSOLE);
    }
}
