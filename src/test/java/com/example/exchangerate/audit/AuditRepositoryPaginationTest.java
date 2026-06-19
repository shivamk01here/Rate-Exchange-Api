package com.example.exchangerate.audit;

import com.example.exchangerate.models.ConversionRecord;
import com.example.exchangerate.models.HistoryPageRequest;
import com.example.exchangerate.models.HistoryPageResponse;
import com.example.exchangerate.models.ProviderCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuditRepositoryPaginationTest {

    private AuditRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AuditRepository();
        for (int i = 0; i < 25; i++) {
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
    void findPage_returnsFirstPage() {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(0)
                .size(10)
                .build();

        HistoryPageResponse response = repository.findPage(request);

        assertEquals(10, response.getRecords().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(25, response.getTotalRecords());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    void findPage_returnsSecondPage() {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(1)
                .size(10)
                .build();

        HistoryPageResponse response = repository.findPage(request);

        assertEquals(10, response.getRecords().size());
        assertEquals(1, response.getPage());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    void findPage_returnsLastPage() {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(2)
                .size(10)
                .build();

        HistoryPageResponse response = repository.findPage(request);

        assertEquals(5, response.getRecords().size());
        assertEquals(2, response.getPage());
        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    void findPage_filtersByCurrencyPair() {
        repository.save(ConversionRecord.builder()
                .id("100")
                .fromCurrency("EUR")
                .toCurrency("USD")
                .status("SUCCESS")
                .timestamp(Instant.now())
                .build());

        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(0)
                .size(10)
                .fromCurrency("EUR")
                .toCurrency("USD")
                .build();

        HistoryPageResponse response = repository.findPage(request);

        assertEquals(1, response.getTotalRecords());
        assertEquals("EUR", response.getRecords().get(0).getFromCurrency());
    }

    @Test
    void findPage_negativePageDefaultsToZero() {
        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(-1)
                .size(10)
                .build();

        HistoryPageResponse response = repository.findPage(request);

        assertEquals(0, response.getPage());
    }

    @Test
    void findPage_emptyRepository() {
        repository = new AuditRepository();

        HistoryPageRequest request = HistoryPageRequest.builder()
                .page(0)
                .size(10)
                .build();

        HistoryPageResponse response = repository.findPage(request);

        assertEquals(0, response.getTotalRecords());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.getRecords().isEmpty());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }
}
