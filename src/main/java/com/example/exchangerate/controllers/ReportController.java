package com.example.exchangerate.controllers;

import com.example.exchangerate.report.ScheduledReport;
import com.example.exchangerate.report.ScheduledReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ScheduledReportService reportService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScheduledReport createReport(@Valid @RequestBody ScheduledReport report) {
        if (report.getName() == null || report.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (report.getCurrencyPairs() == null || report.getCurrencyPairs().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one currency pair is required");
        }
        log.info("Creating report: name={} cron={}", report.getName(), report.getCronExpression());
        return reportService.createReport(report);
    }

    @GetMapping
    public List<ScheduledReport> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{id}")
    public ScheduledReport getReport(@PathVariable String id) {
        return reportService.getReport(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found: " + id));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteReport(@PathVariable String id) {
        boolean deleted = reportService.deleteReport(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found: " + id);
        }
        return Map.of("status", "deleted", "id", id);
    }

    @PatchMapping("/{id}/toggle")
    public ScheduledReport toggleReport(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", true);
        return reportService.toggleReport(id, enabled);
    }

    @GetMapping("/count")
    public Map<String, Object> getReportCount() {
        return Map.of("count", reportService.getReportCount());
    }
}
