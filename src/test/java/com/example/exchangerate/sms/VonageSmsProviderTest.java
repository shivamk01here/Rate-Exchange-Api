package com.example.exchangerate.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VonageSmsProviderTest {

    @Mock
    private SmsProviderFactory factory;

    private VonageSmsProvider provider;
    private SmsConfig smsConfig;

    @BeforeEach
    void setUp() {
        smsConfig = new SmsConfig();
        smsConfig.getVonage().setApiKey("vonage-key");
        smsConfig.getVonage().setApiSecret("vonage-secret");
        smsConfig.getVonage().setPhoneNumber("+14155551234");
        provider = new VonageSmsProvider(smsConfig, factory);
    }

    @Test
    void shouldReturnVonageType() {
        assertThat(provider.getProviderType()).isEqualTo(SmsProviderType.VONAGE);
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
        assertThat(response.getMessageId()).startsWith("VN");
        assertThat(response.getProvider()).isEqualTo(SmsProviderType.VONAGE);
    }
}
