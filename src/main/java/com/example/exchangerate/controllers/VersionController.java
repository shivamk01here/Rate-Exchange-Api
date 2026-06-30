package com.example.exchangerate.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class VersionController {

    @Value("${spring.application.name:exchange-rate-service}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Map.of(
                "service", appName,
                "version", appVersion);
    }
}
