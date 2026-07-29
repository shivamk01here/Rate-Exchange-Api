package com.example.exchangerate.controllers;

import com.example.exchangerate.calculator.CalculatorSummary;
import com.example.exchangerate.calculator.CalculatorSummaryService;
import com.example.exchangerate.config.CalculatorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/calculator/summary")
@RequiredArgsConstructor
public class CalculatorSummaryController {

    private final CalculatorSummaryService summaryService;
    private final CalculatorConfig calculatorConfig;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CalculatorSummary getSummary() {
        if (!calculatorConfig.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Calculator is disabled");
        }
        log.info("Calculator summary requested");
        return summaryService.generateSummary();
    }

    @GetMapping(value = "/pairs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getPairFrequency() {
        if (!calculatorConfig.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Calculator is disabled");
        }
        return summaryService.getPairFrequency();
    }

    @GetMapping(value = "/providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getProviderFrequency() {
        if (!calculatorConfig.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Calculator is disabled");
        }
        return summaryService.getProviderFrequency();
    }
}
