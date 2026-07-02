package com.example.exchangerate.notification;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("notification.email")
public class EmailConfig {

    private boolean enabled = true;
    private String from = "noreply@exchangerate.local";
    private String subjectTemplate = "Rate Alert: {from}->{to}";
}
