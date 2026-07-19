package com.example.exchangerate.alert;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import com.example.exchangerate.notification.EmailService;
import com.example.exchangerate.webhook.WebhookDeliveryService;
import com.example.exchangerate.whatsapp.WhatsAppAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEvaluationService {

    private final AlertRepository alertRepository;
    private final ProviderFactory providerFactory;
    private final EmailService emailService;
    private final WhatsAppAlertService whatsAppAlertService;
    private final WebhookDeliveryService webhookDeliveryService;

    public void evaluateAllAlerts() {
        List<Alert> enabledAlerts = alertRepository.findEnabledAlerts();
        if (enabledAlerts.isEmpty()) {
            return;
        }

        log.debug("Evaluating {} enabled alerts", enabledAlerts.size());

        for (Alert alert : enabledAlerts) {
            try {
                evaluateAlert(alert);
            } catch (Exception e) {
                log.warn("Error evaluating alert {}: {}", alert.getId(), e.getMessage());
            }
        }
    }

    public void evaluateAlert(Alert alert) {
        ExchangeRateProvider provider = providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API);
        ExchangeRateRequest request = ExchangeRateRequest.builder()
                .fromCurrency(alert.getFromCurrency())
                .toCurrency(alert.getToCurrency())
                .amount(BigDecimal.ONE)
                .build();

        CompletableFuture<ExchangeRateResponse> future = provider.fetchRate(request);
        ExchangeRateResponse response = future.join();

        if (!"SUCCESS".equals(response.getStatus()) || response.getRate() == null) {
            log.debug("Skipping alert {}: provider returned status={}", alert.getId(), response.getStatus());
            return;
        }

        BigDecimal currentRate = response.getRate();

        if (alert.shouldTrigger(currentRate)) {
            log.info("Alert {} triggered: {}->{} {} current={} threshold={}",
                    alert.getId(), alert.getFromCurrency(), alert.getToCurrency(),
                    alert.getCondition(), currentRate, alert.getThreshold());

            emailService.sendRateAlert(alert, currentRate);
            whatsAppAlertService.sendRateAlert(alert, currentRate);
            webhookDeliveryService.deliverAlertTriggered(alert, currentRate);
            alertRepository.updateLastTriggered(alert.getId(), Instant.now());
        }
    }

    public void evaluateAndNotify(ExchangeRateResponse response) {
        if (response == null || !"SUCCESS".equals(response.getStatus()) || response.getRate() == null) {
            return;
        }

        List<Alert> matchingAlerts = alertRepository.findByCurrencyPair(
                response.getFromCurrency(), response.getToCurrency());

        for (Alert alert : matchingAlerts) {
            if (alert.isEnabled() && alert.shouldTrigger(response.getRate())) {
                log.info("Alert {} triggered inline: rate={}", alert.getId(), response.getRate());
                emailService.sendRateAlert(alert, response.getRate());
                whatsAppAlertService.sendRateAlert(alert, response.getRate());
                alertRepository.updateLastTriggered(alert.getId(), Instant.now());
            }
        }
    }
}
