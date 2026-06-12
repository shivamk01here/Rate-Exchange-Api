package com.example.exchangerate.controllers;

import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.services.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/history")
    public List<ConversionRecord> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return auditService.getHistory(limit);
    }

    @GetMapping("/history/pair")
    public List<ConversionRecord> getHistoryByPair(
            @RequestParam String from,
            @RequestParam String to) {
        return auditService.getHistoryByPair(from, to);
    }

    @GetMapping("/history/provider")
    public List<ConversionRecord> getHistoryByProvider(
            @RequestParam ProviderCodes code) {
        return auditService.getHistoryByProvider(code);
    }

    @GetMapping("/history/range")
    public List<ConversionRecord> getHistoryByTimeRange(
            @RequestParam long fromEpochMillis,
            @RequestParam long toEpochMillis) {
        return auditService.getHistoryByTimeRange(
                Instant.ofEpochMilli(fromEpochMillis),
                Instant.ofEpochMilli(toEpochMillis));
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
                "totalConversions", auditService.getTotalConversions(),
                "successCount", auditService.getSuccessCount(),
                "failureCount", auditService.getFailureCount(),
                "popularPairs", auditService.getPopularPairs(5));
    }
}
