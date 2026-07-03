package com.example.exchangerate.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleSmsProviderTest {

    private ConsoleSmsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ConsoleSmsProvider(new SmsProviderFactory());
    }

    @Test
    void shouldReturnConsoleType() {
        assertThat(provider.getProviderType()).isEqualTo(SmsProviderType.CONSOLE);
    }

    @Test
    void shouldSendSuccessfully() {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test message")
                .build();

        CompletableFuture<SmsResponse> future = provider.send(request);
        SmsResponse response = future.join();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessageId()).startsWith("CON");
        assertThat(response.getProvider()).isEqualTo(SmsProviderType.CONSOLE);
    }
}
