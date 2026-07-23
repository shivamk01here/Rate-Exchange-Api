package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "calculator")
public class CalculatorConfig {

    private boolean enabled = true;
    private int maxHistorySize = 100;
    private int decimalScale = 4;
    private boolean autoCleanEnabled = false;
    private long autoCleanIntervalMs = 3600000;
    private boolean summaryEnabled = true;
    private int summaryTopPairsLimit = 5;
    private int summaryTopProvidersLimit = 5;
}
