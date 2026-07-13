package com.example.exchangerate.controllers;

import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.HistoryPageRequest;
import com.example.exchangerate.models.HistoryPageResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.models.ExportFormat;
import com.example.exchangerate.config.ExportConfig;
import com.example.exchangerate.services.AuditService;
import com.example.exchangerate.services.CsvExportService;
import com.example.exchangerate.services.JsonExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final CsvExportService csvExportService;
    private final JsonExportService jsonExportService;
    private final ExportConfig exportConfig;

    @GetMapping("/history")
    public List<ConversionRecord> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        return auditService.getHistory(limit);
    }

    @GetMapping("/history/page")
    public HistoryPageResponse getHistoryPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fromCurrency,
            @RequestParam(required = false) String toCurrency,
            @RequestParam(required = false) Long fromEpochMillis,
            @RequestParam(required = false) Long toEpochMillis) {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(page)
                .size(size)
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .fromEpochMillis(fromEpochMillis)
                .toEpochMillis(toEpochMillis)
                .build();
        return auditService.getHistoryPage(request);
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

    @GetMapping("/export")
    public ResponseEntity<String> exportHistory(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) String fromCurrency,
            @RequestParam(required = false) String toCurrency,
            @RequestParam(required = false) Long fromEpochMillis,
            @RequestParam(required = false) Long toEpochMillis,
            @RequestParam(defaultValue = "false") boolean pretty,
            @RequestParam(required = false) Set<String> fields) {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(0)
                .size(exportConfig.getMaxExportSize())
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .fromEpochMillis(fromEpochMillis)
                .toEpochMillis(toEpochMillis)
                .build();
        HistoryPageResponse page = auditService.getHistoryPage(request);
        if (format == ExportFormat.JSON) {
            String json = jsonExportService.exportToJson(page.getRecords(), pretty, fields);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conversion-history.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }
        String csv = csvExportService.exportToCsv(page.getRecords());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conversion-history.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) String fromCurrency,
            @RequestParam(required = false) String toCurrency,
            @RequestParam(required = false) Long fromEpochMillis,
            @RequestParam(required = false) Long toEpochMillis) {
        return exportHistory(ExportFormat.CSV, fromCurrency, toCurrency,
                fromEpochMillis, toEpochMillis, false, null);
    }

    @GetMapping("/export/json")
    public ResponseEntity<String> exportJson(
            @RequestParam(required = false) String fromCurrency,
            @RequestParam(required = false) String toCurrency,
            @RequestParam(required = false) Long fromEpochMillis,
            @RequestParam(required = false) Long toEpochMillis,
            @RequestParam(defaultValue = "false") boolean pretty,
            @RequestParam(required = false) Set<String> fields) {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(0)
                .size(exportConfig.getMaxExportSize())
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .fromEpochMillis(fromEpochMillis)
                .toEpochMillis(toEpochMillis)
                .build();
        HistoryPageResponse page = auditService.getHistoryPage(request);
        String json = jsonExportService.exportToJson(page.getRecords(), pretty, fields);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=conversion-history.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
