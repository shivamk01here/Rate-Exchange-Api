package com.example.exchangerate.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class VersionController {

    private final Optional<BuildProperties> buildProperties;

    @Value("${spring.application.name:exchange-rate-service}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    public VersionController(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("service", appName);
        info.put("version", appVersion);
        buildProperties.ifPresent(bp -> {
            info.put("buildTime", bp.getTime()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            info.put("javaVersion", bp.get("java.version"));
        });
        info.put("javaRuntime", System.getProperty("java.version"));
        return info;
    }
}
