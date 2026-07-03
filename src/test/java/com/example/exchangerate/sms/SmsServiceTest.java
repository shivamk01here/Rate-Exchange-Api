package com.example.exchangerate.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private SmsProviderFactory factory;

    @Mock
    private SmsProvider mockProvider;

    private SmsConfig smsConfig;
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        smsConfig = new SmsConfig();
        smsConfig.setEnabled(true);
        smsConfig.setDefaultProvider(SmsProviderType.CONSOLE);
        smsConfig.setFrom("ExchangeRate");
        smsService = new SmsService(factory, smsConfig);
    }

    @Test
    void shouldSendViaDefaultProvider() {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        when(factory.getProvider(SmsProviderType.CONSOLE)).thenReturn(mockProvider);
        when(mockProvider.send(any())).thenReturn(CompletableFuture.completedFuture(
                SmsResponse.success(request, SmsProviderType.CONSOLE, "MSG1")));

        SmsResponse response = smsService.send(request).join();

        assertThat(response.isSuccess()).isTrue();
        verify(factory).getProvider(SmsProviderType.CONSOLE);
    }

    @Test
    void shouldReturnFailureWhenSmsDisabled() {
        smsConfig.setEnabled(false);

        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        SmsResponse response = smsService.send(request).join();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("disabled");
    }

    @Test
    void shouldSendWithSpecifiedProvider() {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        when(factory.getProvider(SmsProviderType.TWILIO)).thenReturn(mockProvider);
        when(mockProvider.send(any())).thenReturn(CompletableFuture.completedFuture(
                SmsResponse.success(request, SmsProviderType.TWILIO, "MSG2")));

        SmsResponse response = smsService.sendWithProvider(request, SmsProviderType.TWILIO).join();

        assertThat(response.isSuccess()).isTrue();
        verify(factory).getProvider(SmsProviderType.TWILIO);
    }
}
