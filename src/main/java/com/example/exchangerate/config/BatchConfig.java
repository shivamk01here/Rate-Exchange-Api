package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("batch")
public class BatchConfig {

    private int maxSize = 10;
    private int maxAmount = 1000000;
}
