package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("watchlist")
public class WatchlistConfig {

    private boolean enabled = true;
    private int maxEntries = 50;
    private int maxLabelLength = 100;
}
