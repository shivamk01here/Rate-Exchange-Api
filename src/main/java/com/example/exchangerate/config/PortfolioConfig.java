package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("portfolio")
public class PortfolioConfig {

    private int maxPortfolios = 20;
    private boolean enabled = true;
    private int maxHoldingsPerPortfolio = 50;
}
