package com.example.exchangerate.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledReportService {

    private final ScheduledReportRepository reportRepository;

    public ScheduledReport createReport(ScheduledReport report) {
        ScheduledReport saved = reportRepository.save(report);
        log.info("ScheduledReport created: id={} name={} cron={} pairs={}",
                saved.getId(), saved.getName(), saved.getCronExpression(), saved.getCurrencyPairs().size());
        return saved;
    }

    public Optional<ScheduledReport> getReport(String id) {
        return reportRepository.findById(id);
    }

    public List<ScheduledReport> getAllReports() {
        return reportRepository.findAll();
    }

    public boolean deleteReport(String id) {
        boolean deleted = reportRepository.deleteById(id);
        if (deleted) {
            log.info("ScheduledReport deleted: id={}", id);
        }
        return deleted;
    }

    public ScheduledReport toggleReport(String id, boolean enabled) {
        return reportRepository.findById(id)
                .map(existing -> {
                    ScheduledReport updated = ScheduledReport.builder()
                            .id(existing.getId())
                            .name(existing.getName())
                            .cronExpression(existing.getCronExpression())
                            .currencyPairs(existing.getCurrencyPairs())
                            .email(existing.getEmail())
                            .enabled(enabled)
                            .createdAt(existing.getCreatedAt())
                            .lastGeneratedAt(existing.getLastGeneratedAt())
                            .build();
                    ScheduledReport saved = reportRepository.save(updated);
                    log.info("ScheduledReport {} toggled to enabled={}", id, enabled);
                    return saved;
                })
                .orElseThrow(() -> new IllegalArgumentException("ScheduledReport not found: " + id));
    }

    public long getReportCount() {
        return reportRepository.count();
    }

    public List<ScheduledReport> getEnabledReports() {
        return reportRepository.findEnabled();
    }
}
