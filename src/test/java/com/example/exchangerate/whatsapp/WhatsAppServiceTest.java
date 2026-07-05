package com.example.exchangerate.whatsapp;

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
class WhatsAppServiceTest {

    @Mock
    private WhatsAppProviderFactory factory;

    private WhatsAppConfig whatsAppConfig;
    private WhatsAppService whatsAppService;

    @BeforeEach
    void setUp() {
        whatsAppConfig = new WhatsAppConfig();
        whatsAppConfig.setEnabled(true);
        whatsAppConfig.setDefaultProvider(WhatsAppProviderType.CONSOLE);
        whatsAppConfig.setFrom("ExchangeRate");
        whatsAppService = new WhatsAppService(factory, whatsAppConfig);
    }

    @Test
    void shouldSendViaDefaultProvider() {
        ConsoleWhatsAppProvider realProvider = new ConsoleWhatsAppProvider(new WhatsAppProviderFactory());
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        when(factory.getProvider(WhatsAppProviderType.CONSOLE)).thenReturn(realProvider);

        WhatsAppResponse response = whatsAppService.send(request).join();

        assertThat(response.isSuccess()).isTrue();
        verify(factory).getProvider(WhatsAppProviderType.CONSOLE);
    }

    @Test
    void shouldReturnFailureWhenWhatsAppDisabled() {
        whatsAppConfig.setEnabled(false);

        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        WhatsAppResponse response = whatsAppService.send(request).join();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("disabled");
    }

    @Test
    void shouldSendWithSpecifiedProvider() {
        ConsoleWhatsAppProvider realProvider = new ConsoleWhatsAppProvider(new WhatsAppProviderFactory());
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        when(factory.getProvider(WhatsAppProviderType.TWILIO)).thenReturn(realProvider);

        WhatsAppResponse response = whatsAppService.sendWithProvider(request, WhatsAppProviderType.TWILIO).join();

        assertThat(response.isSuccess()).isTrue();
        verify(factory).getProvider(WhatsAppProviderType.TWILIO);
    }
}
