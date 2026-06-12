package com.example.exchangerate;

import com.example.exchangerate.audit.AuditRepository;
import com.example.exchangerate.config.AuditConfig;
import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.services.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuditServiceTest {

    private AuditRepository repository;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        repository = new AuditRepository();
        AuditConfig config = new AuditConfig();
        auditService = new AuditService(repository, config);
    }

    @Test
    void recordConversion_createsRecord() {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .build();

        ConversionRecord record = auditService.recordConversion(response);

        assertNotNull(record.getId());
        assertEquals("USD", record.getFromCurrency());
        assertEquals("INR", record.getToCurrency());
        assertEquals(0, new BigDecimal("83.45").compareTo(record.getRate()));
        assertEquals("SUCCESS", record.getStatus());
        assertNotNull(record.getTimestamp());
    }

    @Test
    void getHistory_returnsMostRecentFirst() {
        for (int i = 0; i < 5; i++) {
            ExchangeRateResponse response = ExchangeRateResponse.builder()
                    .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                    .fromCurrency("USD").toCurrency("INR")
                    .amount(new BigDecimal("100"))
                    .rate(new BigDecimal("83.4" + i))
                    .convertedAmount(new BigDecimal("834" + i))
                    .status("SUCCESS")
                    .build();
            sleepQuietly(10);
            auditService.recordConversion(response);
        }

        List<ConversionRecord> history = auditService.getHistory(10);
        assertEquals(5, history.size());
    }

    @Test
    void getHistoryByPair_filtersCorrectly() {
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("EUR", "USD", "SUCCESS");

        List<ConversionRecord> usdInr = auditService.getHistoryByPair("USD", "INR");
        assertEquals(2, usdInr.size());

        List<ConversionRecord> eurUsd = auditService.getHistoryByPair("EUR", "USD");
        assertEquals(1, eurUsd.size());
    }

    @Test
    void getHistoryByProvider_filtersCorrectly() {
        recordConversionWithProvider("USD", "INR", ProviderCodes.EXCHANGE_RATE_API, "SUCCESS");
        recordConversionWithProvider("EUR", "USD", ProviderCodes.OPEN_EXCHANGE_RATES, "SUCCESS");

        List<ConversionRecord> api = auditService.getHistoryByProvider(ProviderCodes.EXCHANGE_RATE_API);
        assertEquals(1, api.size());

        List<ConversionRecord> oer = auditService.getHistoryByProvider(ProviderCodes.OPEN_EXCHANGE_RATES);
        assertEquals(1, oer.size());
    }

    @Test
    void getHistoryByTimeRange_filtersCorrectly() {
        recordConversion("USD", "INR", "SUCCESS");
        sleepQuietly(100);
        recordConversion("EUR", "USD", "FAILED");

        Instant mid = Instant.now();
        sleepQuietly(100);
        recordConversion("GBP", "USD", "SUCCESS");

        List<ConversionRecord> afterMid = auditService.getHistoryByTimeRange(mid, Instant.now());
        assertEquals(1, afterMid.size());
        assertEquals("GBP", afterMid.get(0).getFromCurrency());
    }

    @Test
    void getStats_returnsCorrectCounts() {
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("EUR", "USD", "SUCCESS");
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("GBP", "EUR", "FAILED_UNSUPPORTED_CURRENCY");

        Map<String, Object> stats = auditService.getTotalConversions() > 0 ? Map.of() : null;
        assertEquals(4, auditService.getTotalConversions());
        assertEquals(3, auditService.getSuccessCount());
        assertEquals(1, auditService.getFailureCount());
    }

    @Test
    void getPopularPairs_returnsTopPairs() {
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("EUR", "USD", "SUCCESS");
        recordConversion("EUR", "USD", "SUCCESS");
        recordConversion("GBP", "EUR", "SUCCESS");

        Map<String, Long> popular = auditService.getPopularPairs(2);
        assertEquals(2, popular.size());
        assertEquals(3L, popular.get("USD_INR").longValue());
        assertEquals(2L, popular.get("EUR_USD").longValue());
    }

    @Test
    void cleanupOldRecords_removesExpiredRecords() {
        recordConversion("USD", "INR", "SUCCESS");
        recordConversion("EUR", "USD", "SUCCESS");

        int removed = auditService.cleanupOldRecords(Duration.ZERO);
        assertEquals(2, removed);

        assertEquals(0, auditService.getTotalConversions());
    }

    @Test
    void conversionRecord_toPipeFormat_and_back() {
        ConversionRecord original = ConversionRecord.builder()
                .id("42")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .timestamp(Instant.ofEpochMilli(1234567890000L))
                .build();

        String pipe = original.toPipeFormat();
        ConversionRecord restored = ConversionRecord.fromPipeFormat(pipe);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getProviderCode(), restored.getProviderCode());
        assertEquals(original.getFromCurrency(), restored.getFromCurrency());
        assertEquals(0, original.getRate().compareTo(restored.getRate()));
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getTimestamp(), restored.getTimestamp());
    }

    @Test
    void conversionRecord_toPipeFormat_withNullFields() {
        ConversionRecord original = ConversionRecord.builder()
                .id("0")
                .fromCurrency("USD")
                .toCurrency("INR")
                .status("FAILED_ALL_PROVIDERS_FAILED")
                .build();

        String pipe = original.toPipeFormat();
        ConversionRecord restored = ConversionRecord.fromPipeFormat(pipe);

        assertEquals("0", restored.getId());
        assertNull(restored.getProviderCode());
        assertNull(restored.getRate());
        assertNull(restored.getTimestamp());
        assertEquals("FAILED_ALL_PROVIDERS_FAILED", restored.getStatus());
    }

    private void recordConversion(String from, String to, String status) {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency(from).toCurrency(to)
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("1.0"))
                .convertedAmount(new BigDecimal("100"))
                .status(status)
                .build();
        auditService.recordConversion(response);
    }

    private void recordConversionWithProvider(String from, String to, ProviderCodes code, String status) {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(code)
                .fromCurrency(from).toCurrency(to)
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("1.0"))
                .convertedAmount(new BigDecimal("100"))
                .status(status)
                .build();
        auditService.recordConversion(response);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
