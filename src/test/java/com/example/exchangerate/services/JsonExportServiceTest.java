package com.example.exchangerate.services;

import com.example.exchangerate.config.ExportConfig;
import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ProviderCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

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
}
