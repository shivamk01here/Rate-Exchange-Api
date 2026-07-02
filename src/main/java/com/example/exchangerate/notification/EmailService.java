package com.example.exchangerate.notification;

import com.example.exchangerate.alert.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    public void sendRateAlert(Alert alert, BigDecimal currentRate) {
        if (!emailConfig.isEnabled()) {
            log.debug("Email notifications disabled, skipping alert {}", alert.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom());
            message.setTo(alert.getEmail());
            message.setSubject(buildSubject(alert));
            message.setText(buildBody(alert, currentRate));

            mailSender.send(message);
            log.info("Alert email sent to {} for alert {} ({}->{} rate={})",
                    alert.getEmail(), alert.getId(),
                    alert.getFromCurrency(), alert.getToCurrency(), currentRate);
        } catch (Exception e) {
            log.error("Failed to send alert email to {} for alert {}: {}",
                    alert.getEmail(), alert.getId(), e.getMessage());
        }
    }

    private String buildSubject(Alert alert) {
        return emailConfig.getSubjectTemplate()
                .replace("{from}", alert.getFromCurrency())
                .replace("{to}", alert.getToCurrency());
    }

    private String buildBody(Alert alert, BigDecimal currentRate) {
        return String.format("""
                Rate Alert Triggered
                
                Currency Pair: %s -> %s
                Condition: %s
                Threshold: %s
                Current Rate: %s
                
                This alert was triggered because the current rate %s %.4f.
                """,
                alert.getFromCurrency(), alert.getToCurrency(),
                alert.getCondition(), alert.getThreshold().toPlainString(),
                currentRate.toPlainString(),
                alert.getCondition() == Alert.AlertCondition.RATE_ABOVE ? "exceeded" : "fell below",
                alert.getThreshold());
    }
}
