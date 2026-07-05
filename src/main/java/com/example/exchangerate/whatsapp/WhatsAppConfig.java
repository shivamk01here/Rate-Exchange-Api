package com.example.exchangerate.whatsapp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("notification.whatsapp")
public class WhatsAppConfig {

    private boolean enabled = false;
    private WhatsAppProviderType defaultProvider = WhatsAppProviderType.CONSOLE;
    private String from = "ExchangeRate";

    private Twilio twilio = new Twilio();

    @Data
    public static class Twilio {
        private String accountSid;
        private String authToken;
        private String phoneNumber;
    }
}
