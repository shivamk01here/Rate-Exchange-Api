package com.example.exchangerate.whatsapp;

import com.example.exchangerate.alert.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppAlertService {

    private final WhatsAppService whatsAppService;
    private final WhatsAppConfig whatsAppConfig;

    public CompletableFuture<WhatsAppResponse> sendRateAlert(Alert alert, BigDecimal currentRate) {
        if (!whatsAppConfig.isEnabled() || alert.getPhone() == null || alert.getPhone().isBlank()) {
            log.debug("WhatsApp alert skipped for alert {} (waEnabled={}, phone={})",
                    alert.getId(), whatsAppConfig.isEnabled(), alert.getPhone());
            return CompletableFuture.completedFuture(null);
        }

        String message = buildWhatsAppBody(alert, currentRate);
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to(alert.getPhone())
                .message(message)
                .build();

        log.info("Sending WhatsApp alert for alert {} to {}", alert.getId(), alert.getPhone());
        return whatsAppService.send(request);
    }

    private String buildWhatsAppBody(Alert alert, BigDecimal currentRate) {
        return String.format("Rate Alert: %s/%s %s %.4f (current: %.4f)",
                alert.getFromCurrency(), alert.getToCurrency(),
                alert.getCondition() == Alert.AlertCondition.RATE_ABOVE ? ">" : "<",
                alert.getThreshold(), currentRate);
    }
}
