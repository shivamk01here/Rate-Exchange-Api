package com.example.exchangerate.controllers;

import com.example.exchangerate.audit.AuditRepository;
import com.example.exchangerate.config.AuditConfig;
import com.example.exchangerate.config.ExportConfig;
import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.HistoryPageRequest;
import com.example.exchangerate.models.HistoryPageResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.services.AuditService;
import com.example.exchangerate.services.CsvExportService;
import com.example.exchangerate.services.JsonExportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditControllerTest {

    private AuditController controller;
    private AuditRepository repository;
    private AuditService auditService;
    private CsvExportService csvExportService;
    private JsonExportService jsonExportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository = new AuditRepository();
        auditService = new AuditService(repository, new AuditConfig());
        csvExportService = new CsvExportService();
        jsonExportService = new JsonExportService(new ExportConfig());
        controller = new AuditController(auditService, csvExportService, jsonExportService, new ExportConfig());
        objectMapper = new ObjectMapper();

        for (int i = 0; i < 15; i++) {
            ConversionRecord record = ConversionRecord.builder()
                    .id(String.valueOf(i))
                    .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                    .fromCurrency("USD")
                    .toCurrency("INR")
                    .amount(new BigDecimal("100"))
                    .rate(new BigDecimal("83.45"))
                    .convertedAmount(new BigDecimal("8345.00"))
                    .status("SUCCESS")
                    .timestamp(Instant.ofEpochMilli(1000 + i))
                    .build();
            repository.save(record);
        }
    }

    @Test
    void getHistoryPage_returnsFirstPage() {
        HistoryPageResponse response = controller.getHistoryPage(0, 5, null, null, null, null);

        assertEquals(5, response.getRecords().size());
        assertEquals(0, response.getPage());
        assertEquals(15, response.getTotalRecords());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    void getHistoryPage_returnsLastPage() {
        HistoryPageResponse response = controller.getHistoryPage(2, 5, null, null, null, null);

        assertEquals(5, response.getRecords().size());
        assertTrue(response.isHasPrevious());
        assertFalse(response.isHasNext());
    }

    @Test
    void getHistoryPage_filtersByCurrency() {
        repository.save(ConversionRecord.builder()
                .id("100").fromCurrency("EUR").toCurrency("USD")
                .status("SUCCESS").timestamp(Instant.now())
                .build());

        HistoryPageResponse response = controller.getHistoryPage(0, 10, "EUR", "USD", null, null);

        assertEquals(1, response.getTotalRecords());
    }

    @Test
    void getHistoryPage_emptyPage() {
        HistoryPageResponse response = controller.getHistoryPage(10, 5, null, null, null, null);

        assertTrue(response.getRecords().isEmpty());
    }

    @Test
    void exportCsv_returnsAllRecords() {
        var response = controller.exportCsv(null, null, null, null);
        String csv = response.getBody();

        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,Provider,From,To,Amount,Rate,ConvertedAmount,Status,Timestamp"));
        String[] lines = csv.split("\n");
        assertEquals(16, lines.length);
    }

    @Test
    void exportCsv_filtersByCurrencyPair() {
        repository.save(ConversionRecord.builder()
                .id("100").fromCurrency("EUR").toCurrency("USD")
                .status("SUCCESS").timestamp(Instant.ofEpochMilli(9999))
                .build());

        var response = controller.exportCsv("EUR", "USD", null, null);
        String csv = response.getBody();

        assertNotNull(csv);
        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);
        assertTrue(lines[1].contains("EUR"));
    }

    @Test
    void exportCsv_returnsAttachmentHeader() {
        var response = controller.exportCsv(null, null, null, null);

        assertTrue(response.getHeaders().containsKey("Content-Disposition"));
        assertEquals("attachment; filename=conversion-history.csv",
                response.getHeaders().get("Content-Disposition").get(0));
    }

    @Test
    void exportCsv_emptyHistoryReturnsOnlyHeader() {
        repository = new AuditRepository();
        auditService = new AuditService(repository, new AuditConfig());
        controller = new AuditController(auditService, csvExportService, jsonExportService, new ExportConfig());

        var response = controller.exportCsv(null, null, null, null);
        String csv = response.getBody();

        assertNotNull(csv);
        String[] lines = csv.split("\n");
        assertEquals(1, lines.length);
        assertEquals("ID,Provider,From,To,Amount,Rate,ConvertedAmount,Status,Timestamp", lines[0]);
    }

    @Test
    void exportJson_returnsAllRecords() throws Exception {
        var response = controller.exportJson(null, null, null, null, false, null);
        String json = response.getBody();

        assertNotNull(json);
        List<Map<String, Object>> parsed = objectMapper.readValue(json, new TypeReference<>() {});
        assertEquals(15, parsed.size());
    }

    @Test
    void exportJson_filtersByCurrencyPair() throws Exception {
        repository.save(ConversionRecord.builder()
                .id("100").fromCurrency("EUR").toCurrency("USD")
                .status("SUCCESS").timestamp(Instant.ofEpochMilli(9999))
                .build());

        var response = controller.exportJson("EUR", "USD", null, null, false, null);
        String json = response.getBody();

        assertNotNull(json);
        List<Map<String, Object>> parsed = objectMapper.readValue(json, new TypeReference<>() {});
        assertEquals(1, parsed.size());
        assertEquals("EUR", parsed.get(0).get("fromCurrency"));
    }

    @Test
    void exportJson_returnsAttachmentHeader() {
        var response = controller.exportJson(null, null, null, null, false, null);

        assertTrue(response.getHeaders().containsKey("Content-Disposition"));
        assertEquals("attachment; filename=conversion-history.json",
                response.getHeaders().get("Content-Disposition").get(0));
    }

    @Test
    void exportJson_emptyHistoryReturnsEmptyArray() {
        repository = new AuditRepository();
        auditService = new AuditService(repository, new AuditConfig());
        controller = new AuditController(auditService, csvExportService, jsonExportService, new ExportConfig());

        var response = controller.exportJson(null, null, null, null, false, null);
        String json = response.getBody();

        assertEquals("[]", json);
    }
}
