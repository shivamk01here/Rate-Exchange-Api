package com.example.exchangerate.sms;

import com.example.exchangerate.alert.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsAlertService {

    private final SmsService smsService;
    private final SmsConfig smsConfig;

    public CompletableFuture<SmsResponse> sendRateAlert(Alert alert, BigDecimal currentRate) {
        if (!smsConfig.isEnabled() || alert.getPhone() == null || alert.getPhone().isBlank()) {
            log.debug("SMS alert skipped for alert {} (smsEnabled={}, phone={})",
                    alert.getId(), smsConfig.isEnabled(), alert.getPhone());
            return CompletableFuture.completedFuture(null);
        }

        String message = buildSmsBody(alert, currentRate);
        SmsRequest request = SmsRequest.builder()
                .to(alert.getPhone())
                .message(message)
                .build();

        log.info("Sending SMS alert for alert {} to {}", alert.getId(), alert.getPhone());
        return smsService.send(request);
    }

    private String buildSmsBody(Alert alert, BigDecimal currentRate) {
        return String.format("Rate Alert: %s/%s %s %.4f (current: %.4f)",
                alert.getFromCurrency(), alert.getToCurrency(),
                alert.getCondition() == Alert.AlertCondition.RATE_ABOVE ? ">" : "<",
                alert.getThreshold(), currentRate);
    }
}
