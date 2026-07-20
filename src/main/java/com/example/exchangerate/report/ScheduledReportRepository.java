package com.example.exchangerate.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ScheduledReportRepository {

    private final ConcurrentHashMap<String, ScheduledReport> reports = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<ScheduledReport> reportList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public ScheduledReport save(ScheduledReport report) {
        String id = report.getId() != null ? report.getId() : String.valueOf(idCounter.incrementAndGet());
        ScheduledReport stored = ScheduledReport.builder()
                .id(id)
                .name(report.getName())
                .cronExpression(report.getCronExpression())
                .currencyPairs(report.getCurrencyPairs())
                .email(report.getEmail())
                .enabled(report.isEnabled())
                .createdAt(report.getCreatedAt() != null ? report.getCreatedAt() : java.time.Instant.now())
                .lastGeneratedAt(report.getLastGeneratedAt())
                .build();

        if (reports.putIfAbsent(id, stored) == null) {
            reportList.add(stored);
        } else {
            reports.put(id, stored);
            for (int i = 0; i < reportList.size(); i++) {
                if (id.equals(reportList.get(i).getId())) {
                    reportList.set(i, stored);
                    break;
                }
            }
        }

        log.debug("ScheduledReport saved: id={} name={}", id, stored.getName());
        return stored;
    }

    public Optional<ScheduledReport> findById(String id) {
        return Optional.ofNullable(reports.get(id));
    }

    public List<ScheduledReport> findAll() {
        return new ArrayList<>(reportList);
    }

    public List<ScheduledReport> findEnabled() {
        return reportList.stream()
                .filter(ScheduledReport::isEnabled)
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean deleteById(String id) {
        ScheduledReport removed = reports.remove(id);
        if (removed != null) {
            reportList.remove(removed);
            log.info("ScheduledReport deleted: id={}", id);
            return true;
        }
        return false;
    }

    public long count() {
        return reports.size();
    }

    public void updateLastGenerated(String id, java.time.Instant timestamp) {
        ScheduledReport existing = reports.get(id);
        if (existing != null) {
            ScheduledReport updated = ScheduledReport.builder()
                    .id(existing.getId())
                    .name(existing.getName())
                    .cronExpression(existing.getCronExpression())
                    .currencyPairs(existing.getCurrencyPairs())
                    .email(existing.getEmail())
                    .enabled(existing.isEnabled())
                    .createdAt(existing.getCreatedAt())
                    .lastGeneratedAt(timestamp)
                    .build();
            reports.put(id, updated);
            for (int i = 0; i < reportList.size(); i++) {
                if (id.equals(reportList.get(i).getId())) {
                    reportList.set(i, updated);
                    break;
                }
            }
        }
    }
}
