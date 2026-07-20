package com.example.exchangerate.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledReportServiceTest {

    private ScheduledReportService reportService;
    private ScheduledReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        reportRepository = new ScheduledReportRepository();
        reportService = new ScheduledReportService(reportRepository);
    }

    @Test
    void createReport_returnsSavedReportWithId() {
        ScheduledReport report = ScheduledReport.builder()
                .name("Daily INR")
                .cronExpression("0 8 * * * ?")
                .currencyPairs(List.of(
                        new ScheduledReport.CurrencyPair("USD", "INR"),
                        new ScheduledReport.CurrencyPair("EUR", "INR")
                ))
                .email("user@example.com")
                .enabled(true)
                .build();

        ScheduledReport saved = reportService.createReport(report);

        assertNotNull(saved.getId());
        assertEquals("Daily INR", saved.getName());
        assertTrue(saved.isEnabled());
    }

    @Test
    void getReport_returnsReportWhenExists() {
        ScheduledReport saved = reportService.createReport(ScheduledReport.builder()
                .name("Test Report").cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "EUR")))
                .email("test@example.com").enabled(true).build());

        ScheduledReport found = reportService.getReport(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void getReport_returnsEmptyWhenNotFound() {
        assertTrue(reportService.getReport("nonexistent").isEmpty());
    }

    @Test
    void getAllReports_returnsAllCreatedReports() {
        reportService.createReport(ScheduledReport.builder().name("R1")
                .cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "EUR")))
                .email("a@b.com").enabled(true).build());
        reportService.createReport(ScheduledReport.builder().name("R2")
                .cronExpression("0 30 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("GBP", "USD")))
                .email("c@d.com").enabled(false).build());

        List<ScheduledReport> all = reportService.getAllReports();

        assertEquals(2, all.size());
    }

    @Test
    void deleteReport_removesReport() {
        ScheduledReport saved = reportService.createReport(ScheduledReport.builder()
                .name("Delete Me").cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "JPY")))
                .email("x@y.com").enabled(true).build());

        assertTrue(reportService.deleteReport(saved.getId()));
        assertTrue(reportService.getReport(saved.getId()).isEmpty());
    }

    @Test
    void deleteReport_returnsFalseForNonexistent() {
        assertFalse(reportService.deleteReport("nonexistent"));
    }

    @Test
    void toggleReport_enablesAndDisables() {
        ScheduledReport saved = reportService.createReport(ScheduledReport.builder()
                .name("Toggle").cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "CAD")))
                .email("z@z.com").enabled(false).build());

        ScheduledReport toggledOn = reportService.toggleReport(saved.getId(), true);
        assertTrue(toggledOn.isEnabled());

        ScheduledReport toggledOff = reportService.toggleReport(saved.getId(), false);
        assertFalse(toggledOff.isEnabled());
    }

    @Test
    void getReportCount_returnsCorrectCount() {
        assertEquals(0, reportService.getReportCount());

        reportService.createReport(ScheduledReport.builder().name("Count")
                .cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "EUR")))
                .email("a@b.com").enabled(true).build());

        assertEquals(1, reportService.getReportCount());
    }

    @Test
    void getEnabledReports_returnsOnlyEnabledReports() {
        reportService.createReport(ScheduledReport.builder().name("Enabled")
                .cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "EUR")))
                .email("a@b.com").enabled(true).build());
        reportService.createReport(ScheduledReport.builder().name("Disabled")
                .cronExpression("0 30 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("GBP", "USD")))
                .email("c@d.com").enabled(false).build());

        List<ScheduledReport> enabled = reportService.getEnabledReports();

        assertEquals(1, enabled.size());
        assertEquals("Enabled", enabled.get(0).getName());
    }
}
