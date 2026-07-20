package com.example.exchangerate.controllers;

import com.example.exchangerate.report.ScheduledReport;
import com.example.exchangerate.report.ScheduledReportRepository;
import com.example.exchangerate.report.ScheduledReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportControllerTest {

    private ReportController controller;

    @BeforeEach
    void setUp() {
        ScheduledReportRepository repository = new ScheduledReportRepository();
        ScheduledReportService service = new ScheduledReportService(repository);
        controller = new ReportController(service);
    }

    @Test
    void createReport_returnsCreatedReport() {
        ScheduledReport report = ScheduledReport.builder()
                .name("Test Report")
                .cronExpression("0 8 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "INR")))
                .email("user@example.com")
                .enabled(true)
                .build();

        ScheduledReport result = controller.createReport(report);

        assertNotNull(result.getId());
        assertEquals("Test Report", result.getName());
    }

    @Test
    void createReport_throwsWhenNameMissing() {
        ScheduledReport report = ScheduledReport.builder()
                .cronExpression("0 8 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "INR")))
                .email("user@example.com")
                .enabled(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.createReport(report));
    }

    @Test
    void getAllReports_returnsAll() {
        controller.createReport(ScheduledReport.builder().name("R1")
                .cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "EUR")))
                .email("a@b.com").enabled(true).build());
        controller.createReport(ScheduledReport.builder().name("R2")
                .cronExpression("0 30 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("GBP", "USD")))
                .email("c@d.com").enabled(false).build());

        List<ScheduledReport> all = controller.getAllReports();

        assertEquals(2, all.size());
    }

    @Test
    void getReport_returnsById() {
        ScheduledReport created = controller.createReport(ScheduledReport.builder()
                .name("Find").cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "JPY")))
                .email("x@y.com").enabled(true).build());

        ScheduledReport result = controller.getReport(created.getId());

        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getReport_throwsForNonexistent() {
        assertThrows(ResponseStatusException.class, () -> controller.getReport("bad-id"));
    }

    @Test
    void deleteReport_returnsSuccess() {
        ScheduledReport created = controller.createReport(ScheduledReport.builder()
                .name("Delete").cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "CAD")))
                .email("z@z.com").enabled(true).build());

        Map<String, String> result = controller.deleteReport(created.getId());

        assertEquals("deleted", result.get("status"));
    }

    @Test
    void toggleReport_changesEnabledState() {
        ScheduledReport created = controller.createReport(ScheduledReport.builder()
                .name("Toggle").cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "GBP")))
                .email("t@t.com").enabled(false).build());

        ScheduledReport toggled = controller.toggleReport(created.getId(), Map.of("enabled", true));

        assertTrue(toggled.isEnabled());
    }

    @Test
    void getReportCount_returnsCount() {
        controller.createReport(ScheduledReport.builder().name("Count")
                .cronExpression("0 0 * * * ?")
                .currencyPairs(List.of(new ScheduledReport.CurrencyPair("USD", "EUR")))
                .email("a@b.com").enabled(true).build());

        Map<String, Object> result = controller.getReportCount();

        assertEquals(1L, result.get("count"));
    }
}
