package com.example.exchangerate.services;

import com.example.exchangerate.config.ExportConfig;
import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ProviderCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JsonExportServiceTest {

    private JsonExportService jsonExportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ExportConfig config = new ExportConfig();
        jsonExportService = new JsonExportService(config);
        objectMapper = new ObjectMapper();
    }

    @Test
    void exportToJson_returnsEmptyArrayForEmptyList() {
        String json = jsonExportService.exportToJson(Collections.emptyList());
        assertEquals("[]", json);
    }

    @Test
    void exportToJson_returnsEmptyArrayForNullList() {
        String json = jsonExportService.exportToJson(null);
        assertEquals("[]", json);
    }

    @Test
    void exportToJson_serializesSingleRecord() throws Exception {
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

        String json = jsonExportService.exportToJson(List.of(record));

        assertTrue(json.contains("USD"));
        assertTrue(json.contains("INR"));
        assertTrue(json.contains("83.45"));
        assertTrue(json.contains("EXCHANGE_RATE_API"));
    }

    @Test
    void exportToJson_serializesMultipleRecords() throws Exception {
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

        String json = jsonExportService.exportToJson(List.of(r1, r2));

        List<Map<String, Object>> parsed = objectMapper.readValue(json, new TypeReference<>() {});
        assertEquals(2, parsed.size());
        assertEquals("USD", parsed.get(0).get("fromCurrency"));
        assertEquals("EUR", parsed.get(1).get("fromCurrency"));
    }

    @Test
    void exportToJson_handlesNullFields() throws Exception {
        ConversionRecord record = ConversionRecord.builder()
                .id("0")
                .fromCurrency("USD")
                .toCurrency("INR")
                .status("FAILED_ALL_PROVIDERS_FAILED")
                .build();

        String json = jsonExportService.exportToJson(List.of(record));

        Map<String, Object> parsed = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {}).get(0);
        assertEquals("0", parsed.get("id"));
        assertNull(parsed.get("providerCode"));
        assertNull(parsed.get("amount"));
    }

    @Test
    void exportToJson_prettyPrintIncludesNewlines() {
        ConversionRecord record = ConversionRecord.builder()
                .id("1").fromCurrency("USD").toCurrency("INR")
                .amount(new BigDecimal("100")).rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00")).status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(1000))
                .build();

        String json = jsonExportService.exportToJson(List.of(record), true);

        assertTrue(json.contains("\n"));
    }

    @Test
    void exportToJson_fieldSelectionOnlyIncludesRequestedFields() throws Exception {
        ConversionRecord record = ConversionRecord.builder()
                .id("1").fromCurrency("USD").toCurrency("INR")
                .amount(new BigDecimal("100")).rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00")).status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(1000))
                .build();

        String json = jsonExportService.exportToJson(List.of(record), false, Set.of("fromCurrency", "toCurrency"));

        Map<String, Object> parsed = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {}).get(0);
        assertEquals("USD", parsed.get("fromCurrency"));
        assertEquals("INR", parsed.get("toCurrency"));
        assertNull(parsed.get("id"));
        assertNull(parsed.get("amount"));
        assertEquals(2, parsed.size());
    }
}
