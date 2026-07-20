package com.example.exchangerate.report;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportScheduler {

    private final ScheduledReportRepository reportRepository;
    private final ProviderFactory providerFactory;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @PostConstruct
    public void init() {
        List<ScheduledReport> enabled = reportRepository.findEnabled();
        for (ScheduledReport report : enabled) {
            scheduleReport(report);
        }
    }

    public void scheduleReport(ScheduledReport report) {
        cancelReport(report.getId());
        if (!report.isEnabled()) return;

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> generateReport(report),
                new CronTrigger(report.getCronExpression())
        );
        scheduledTasks.put(report.getId(), future);
        log.info("Report scheduled: id={} name={} cron={}", report.getId(), report.getName(), report.getCronExpression());
    }

    public void cancelReport(String id) {
        ScheduledFuture<?> existing = scheduledTasks.remove(id);
        if (existing != null) {
            existing.cancel(false);
            log.info("Report schedule cancelled: id={}", id);
        }
    }

    public void generateReport(ScheduledReport report) {
        log.info("Generating report: id={} name={}", report.getId(), report.getName());
        StringBuilder body = new StringBuilder();
        body.append("Scheduled Rate Report: ").append(report.getName()).append("\n\n");

        for (ScheduledReport.CurrencyPair pair : report.getCurrencyPairs()) {
            try {
                ExchangeRateProvider provider = providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API);
                ExchangeRateRequest request = ExchangeRateRequest.builder()
                        .fromCurrency(pair.getFrom())
                        .toCurrency(pair.getTo())
                        .amount(BigDecimal.ONE)
                        .build();

                CompletableFuture<ExchangeRateResponse> future = provider.fetchRate(request);
                ExchangeRateResponse response = future.join();

                if ("SUCCESS".equals(response.getStatus()) && response.getRate() != null) {
                    body.append(pair.getFrom()).append("/").append(pair.getTo())
                            .append(" = ").append(response.getRate())
                            .append(" (provider: ").append(response.getProviderCode()).append(")\n");
                } else {
                    body.append(pair.getFrom()).append("/").append(pair.getTo())
                            .append(" = FAILED\n");
                }
            } catch (Exception e) {
                log.warn("Failed to fetch rate for {}->{}: {}", pair.getFrom(), pair.getTo(), e.getMessage());
                body.append(pair.getFrom()).append("/").append(pair.getTo())
                        .append(" = ERROR: ").append(e.getMessage()).append("\n");
            }
        }

        body.append("\nGenerated at: ").append(Instant.now());

        emailService.sendSimpleMessage(report.getEmail(), "Rate Report: " + report.getName(), body.toString());
        reportRepository.updateLastGenerated(report.getId(), Instant.now());
        log.info("Report generated and sent: id={} name={} to={}", report.getId(), report.getName(), report.getEmail());
    }
}
