package com.example.exchangerate;

import com.example.exchangerate.models.BatchConversionRequest;
import com.example.exchangerate.models.BatchConversionResponse;
import com.example.exchangerate.models.BatchRateResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchConversionTest {

    @Test
    void batchConversionRequest_createsSuccessfully() {
        BatchConversionRequest request = BatchConversionRequest.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .toCurrencies(List.of("INR", "EUR", "GBP"))
                .build();

        assertEquals("USD", request.getFromCurrency());
        assertEquals(0, new BigDecimal("100").compareTo(request.getAmount()));
        assertEquals(3, request.getToCurrencies().size());
        assertTrue(request.getToCurrencies().contains("INR"));
        assertTrue(request.getToCurrencies().contains("EUR"));
        assertTrue(request.getToCurrencies().contains("GBP"));
    }

    @Test
    void batchRateResult_createsSuccessfully() {
        BatchRateResult result = BatchRateResult.builder()
                .toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .build();

        assertEquals("INR", result.getToCurrency());
        assertEquals(0, new BigDecimal("83.45").compareTo(result.getRate()));
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    void batchRateResult_handlesFailureStatus() {
        BatchRateResult result = BatchRateResult.builder()
                .toCurrency("XYZ")
                .status("FAILED_UNSUPPORTED_CURRENCY")
                .build();

        assertEquals("FAILED_UNSUPPORTED_CURRENCY", result.getStatus());
        assertNull(result.getRate());
        assertNull(result.getConvertedAmount());
    }

    @Test
    void batchConversionResponse_aggregatesResults() {
        BatchRateResult inr = BatchRateResult.builder()
                .toCurrency("INR").rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00")).status("SUCCESS")
                .build();

        BatchRateResult eur = BatchRateResult.builder()
                .toCurrency("EUR").rate(new BigDecimal("0.92"))
                .convertedAmount(new BigDecimal("92.00")).status("SUCCESS")
                .build();

        BatchConversionResponse response = BatchConversionResponse.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .results(List.of(inr, eur))
                .build();

        assertEquals("USD", response.getFromCurrency());
        assertEquals(2, response.getResults().size());
        assertEquals("INR", response.getResults().get(0).getToCurrency());
        assertEquals("EUR", response.getResults().get(1).getToCurrency());
    }

    @Test
    void batchConversionRequest_noArgConstructorWorks() {
        BatchConversionRequest request = new BatchConversionRequest();
        request.setFromCurrency("EUR");
        request.setAmount(new BigDecimal("200"));
        request.setToCurrencies(List.of("USD", "GBP"));

        assertEquals("EUR", request.getFromCurrency());
        assertEquals(2, request.getToCurrencies().size());
    }

    @Test
    void batchConversionResponse_withEmptyResults() {
        BatchConversionResponse response = BatchConversionResponse.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .results(List.of())
                .build();

        assertEquals("USD", response.getFromCurrency());
        assertTrue(response.getResults().isEmpty());
    }
}
