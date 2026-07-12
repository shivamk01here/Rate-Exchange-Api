package com.example.exchangerate.trend;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("rate-trend")
public class RateTrendConfig {

    private boolean enabled = true;
    private int maxSnapshotsPerPair = 500;
    private int displayLimit = 10;
    private double stabilityThresholdPercent = 0.5;
    private int cleanupIntervalMinutes = 60;
}
