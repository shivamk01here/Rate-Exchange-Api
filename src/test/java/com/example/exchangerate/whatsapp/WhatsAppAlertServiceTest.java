package com.example.exchangerate.whatsapp;

import com.example.exchangerate.alert.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppAlertServiceTest {

    @Mock
    private WhatsAppService whatsAppService;

    private WhatsAppConfig whatsAppConfig;
    private WhatsAppAlertService whatsAppAlertService;

    @Captor
    private ArgumentCaptor<WhatsAppRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        whatsAppConfig = new WhatsAppConfig();
        whatsAppConfig.setEnabled(true);
        whatsAppAlertService = new WhatsAppAlertService(whatsAppService, whatsAppConfig);
    }

    @Test
    void shouldSendWhatsAppForAlertWithPhone() {
        Alert alert = Alert.builder()
                .id("alert-1")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("0.8500"))
                .phone("+1234567890")
                .build();

        when(whatsAppService.send(any())).thenReturn(CompletableFuture.completedFuture(
                WhatsAppResponse.builder().success(true).build()));

        whatsAppAlertService.sendRateAlert(alert, new BigDecimal("0.8600")).join();

        verify(whatsAppService).send(requestCaptor.capture());
        WhatsAppRequest captured = requestCaptor.getValue();
        assertThat(captured.getTo()).isEqualTo("+1234567890");
        assertThat(captured.getMessage()).contains("USD").contains("EUR");
    }

    @Test
    void shouldSkipWhenPhoneIsNull() {
        Alert alert = Alert.builder()
                .id("alert-2")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .build();

        WhatsAppResponse result = whatsAppAlertService.sendRateAlert(alert, new BigDecimal("0.8600")).join();

        assertThat(result).isNull();
    }

    @Test
    void shouldSkipWhenWhatsAppDisabled() {
        whatsAppConfig.setEnabled(false);

        Alert alert = Alert.builder()
                .id("alert-3")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .phone("+1234567890")
                .build();

        WhatsAppResponse result = whatsAppAlertService.sendRateAlert(alert, new BigDecimal("0.8600")).join();

        assertThat(result).isNull();
    }
}
