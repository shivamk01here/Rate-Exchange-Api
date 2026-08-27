package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("rate-bookmarks")
public class RateBookmarkConfig {

    private boolean enabled = true;
    private int maxBookmarks = 50;
    private int maxLabelLength = 100;
}
