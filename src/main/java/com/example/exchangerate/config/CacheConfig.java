package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties("cache")
public class CacheConfig {

    private Duration ttl = Duration.ofMinutes(5);
    private int maxSize = 1000;
    private long evictionIntervalMs = 30000;
}
