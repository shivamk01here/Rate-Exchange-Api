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
    private JsonSettings json = new JsonSettings();

    @Data
    public static class JsonSettings {
        private boolean prettyPrint = false;
        private String dateFormat = "iso";
        private boolean includeNullFields = false;
    }
}
