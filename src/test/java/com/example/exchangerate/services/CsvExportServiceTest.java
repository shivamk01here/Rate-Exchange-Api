package com.example.exchangerate.services;

import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ProviderCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvExportServiceTest {

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {
        csvExportService = new CsvExportService();
    }

    @Test
    void exportToCsv_returnsHeaderForEmptyList() {
        String csv = csvExportService.exportToCsv(Collections.emptyList());
        assertEquals("ID,Provider,From,To,Amount,Rate,ConvertedAmount,Status,Timestamp", csv);
    }

    @Test
    void exportToCsv_returnsHeaderForNullList() {
        String csv = csvExportService.exportToCsv(null);
        assertEquals("ID,Provider,From,To,Amount,Rate,ConvertedAmount,Status,Timestamp", csv);
    }

    @Test
    void exportToCsv_includesHeaderAndData() {
        ConversionRecord record = ConversionRecord.builder()
                .id("1")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(1000))
                .build();

        String csv = csvExportService.exportToCsv(List.of(record));

        assertTrue(csv.startsWith("ID,Provider,From,To,Amount,Rate,ConvertedAmount,Status,Timestamp"));
        assertTrue(csv.contains("1,EXCHANGE_RATE_API,USD,INR,100,83.45,8345.00,SUCCESS,1970-01-01T00:00:01Z"));
    }

    @Test
    void exportToCsv_handlesMultipleRecords() {
        ConversionRecord r1 = ConversionRecord.builder()
                .id("1").fromCurrency("USD").toCurrency("INR")
                .amount(new BigDecimal("100")).rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00")).status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(1000))
                .build();

        ConversionRecord r2 = ConversionRecord.builder()
                .id("2").fromCurrency("EUR").toCurrency("USD")
                .amount(new BigDecimal("200")).rate(new BigDecimal("1.10"))
                .convertedAmount(new BigDecimal("220.00")).status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(2000))
                .build();

        String csv = csvExportService.exportToCsv(List.of(r1, r2));
        String[] lines = csv.split("\n");

        assertEquals(3, lines.length);
        assertTrue(lines[1].contains("1,EXCHANGE_RATE_API"));
        assertTrue(lines[2].contains("2,EXCHANGE_RATE_API"));
    }

    @Test
    void exportToCsv_handlesNullFields() {
        ConversionRecord record = ConversionRecord.builder()
                .id("0")
                .fromCurrency("USD")
                .toCurrency("INR")
                .status("FAILED_ALL_PROVIDERS_FAILED")
                .build();

        String csv = csvExportService.exportToCsv(List.of(record));
        assertTrue(csv.contains("0,,,,,,,FAILED_ALL_PROVIDERS_FAILED,"));
    }

    @Test
    void exportToCsv_escapesCommas() {
        ConversionRecord record = ConversionRecord.builder()
                .id("1")
                .fromCurrency("USD")
                .toCurrency("INR")
                .status("FAILED, TIMEOUT")
                .timestamp(Instant.ofEpochMilli(1000))
                .build();

        String csv = csvExportService.exportToCsv(List.of(record));
        assertTrue(csv.contains("\"FAILED, TIMEOUT\""));
    }

    @Test
    void exportToCsv_handlesMissingProviderCode() {
        ConversionRecord record = ConversionRecord.builder()
                .id("1")
                .fromCurrency("USD").toCurrency("INR")
                .status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(1000))
                .build();

        String csv = csvExportService.exportToCsv(List.of(record));
        assertTrue(csv.contains("1,,USD,INR,,,,SUCCESS,"));
    }
}
