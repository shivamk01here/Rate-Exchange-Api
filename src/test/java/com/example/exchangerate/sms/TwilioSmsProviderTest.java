package com.example.exchangerate.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TwilioSmsProviderTest {

    @Mock
    private SmsProviderFactory factory;

    private TwilioSmsProvider provider;
    private SmsConfig smsConfig;

    @BeforeEach
    void setUp() {
        smsConfig = new SmsConfig();
        smsConfig.getTwilio().setAccountSid("AC1234567890abcdef");
        smsConfig.getTwilio().setAuthToken("tokentoken");
        smsConfig.getTwilio().setPhoneNumber("+14155551234");
        provider = new TwilioSmsProvider(smsConfig, factory);
    }

    @Test
    void shouldReturnTwilioType() {
        assertThat(provider.getProviderType()).isEqualTo(SmsProviderType.TWILIO);
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
        assertThat(response.getMessageId()).startsWith("TW");
        assertThat(response.getProvider()).isEqualTo(SmsProviderType.TWILIO);
    }
}
