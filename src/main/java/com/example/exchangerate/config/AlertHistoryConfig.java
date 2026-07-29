package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alert-history")
public class AlertHistoryConfig {

    private boolean enabled = true;
    private int maxPageSize = 50;
    private int maxEntries = 5000;
    private boolean trackNotifications = true;
}
