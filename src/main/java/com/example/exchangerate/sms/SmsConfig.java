package com.example.exchangerate.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("notification.sms")
public class SmsConfig {

    private boolean enabled = false;
    private SmsProviderType defaultProvider = SmsProviderType.CONSOLE;
    private String from = "ExchangeRate";

    private Twilio twilio = new Twilio();
    private AwsSns awsSns = new AwsSns();
    private Vonage vonage = new Vonage();

    @Data
    public static class Twilio {
        private String accountSid;
        private String authToken;
        private String phoneNumber;
    }

    @Data
    public static class AwsSns {
        private String accessKey;
        private String secretKey;
        private String region = "us-east-1";
    }

    @Data
    public static class Vonage {
        private String apiKey;
        private String apiSecret;
        private String phoneNumber;
    }
}
