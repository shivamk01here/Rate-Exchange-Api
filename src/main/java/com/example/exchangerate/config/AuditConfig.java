package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties("audit")
public class AuditConfig {

    private Duration retention = Duration.ofDays(30);
    private int defaultHistoryLimit = 50;
}
