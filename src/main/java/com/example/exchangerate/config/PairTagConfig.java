package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("pair-tags")
public class PairTagConfig {

    private boolean enabled = true;
    private int maxTags = 200;
    private int maxTagLength = 50;
}
