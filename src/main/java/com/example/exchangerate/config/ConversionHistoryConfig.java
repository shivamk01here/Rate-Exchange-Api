package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "conversion-history")
public class ConversionHistoryConfig {

    private boolean enabled = true;
    private int maxPageSize = 50;
    private int maxEntries = 10000;
    private boolean recordClientInfo = true;
    private boolean autoCleanupEnabled = false;
    private long autoCleanupIntervalMs = 86400000;
    private int retentionDays = 30;
}
