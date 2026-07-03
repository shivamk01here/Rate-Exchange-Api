package com.example.exchangerate.sms;

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
class SmsAlertServiceTest {

    @Mock
    private SmsService smsService;

    private SmsConfig smsConfig;
    private SmsAlertService smsAlertService;

    @Captor
    private ArgumentCaptor<SmsRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        smsConfig = new SmsConfig();
        smsConfig.setEnabled(true);
        smsAlertService = new SmsAlertService(smsService, smsConfig);
    }

    @Test
    void shouldSendSmsForAlertWithPhone() {
        Alert alert = Alert.builder()
                .id("alert-1")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .condition(Alert.AlertCondition.RATE_ABOVE)
                .threshold(new BigDecimal("0.8500"))
                .phone("+1234567890")
                .build();

        when(smsService.send(any())).thenReturn(CompletableFuture.completedFuture(
                SmsResponse.builder().success(true).build()));

        smsAlertService.sendRateAlert(alert, new BigDecimal("0.8600")).join();

        verify(smsService).send(requestCaptor.capture());
        SmsRequest captured = requestCaptor.getValue();
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

        SmsResponse result = smsAlertService.sendRateAlert(alert, new BigDecimal("0.8600")).join();

        assertThat(result).isNull();
    }

    @Test
    void shouldSkipWhenSmsDisabled() {
        smsConfig.setEnabled(false);

        Alert alert = Alert.builder()
                .id("alert-3")
                .fromCurrency("USD")
                .toCurrency("EUR")
                .phone("+1234567890")
                .build();

        SmsResponse result = smsAlertService.sendRateAlert(alert, new BigDecimal("0.8600")).join();

        assertThat(result).isNull();
    }
}
