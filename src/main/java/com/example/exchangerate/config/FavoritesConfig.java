package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("favorites")
public class FavoritesConfig {

    private int maxFavorites = 50;
    private boolean enabled = true;
    private int maxLabelLength = 100;
}
