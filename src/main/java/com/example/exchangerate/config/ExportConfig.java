package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("export")
public class ExportConfig {

    private int maxExportSize = 10000;
    private String filename = "conversion-history";
}
