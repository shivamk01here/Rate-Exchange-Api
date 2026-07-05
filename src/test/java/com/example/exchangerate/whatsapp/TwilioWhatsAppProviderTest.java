package com.example.exchangerate.whatsapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class TwilioWhatsAppProviderTest {

    private TwilioWhatsAppProvider provider;

    @BeforeEach
    void setUp() {
        WhatsAppConfig config = new WhatsAppConfig();
        config.getTwilio().setAccountSid("AC12345678");
        provider = new TwilioWhatsAppProvider(config, new WhatsAppProviderFactory());
    }

    @Test
    void shouldReturnTwilioType() {
        assertThat(provider.getProviderType()).isEqualTo(WhatsAppProviderType.TWILIO);
    }

    @Test
    void shouldSendSuccessfully() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp")
                .build();

        CompletableFuture<WhatsAppResponse> future = provider.send(request);
        WhatsAppResponse response = future.join();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessageId()).startsWith("WATW");
        assertThat(response.getProvider()).isEqualTo(WhatsAppProviderType.TWILIO);
    }
}
