package com.example.exchangerate;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.models.ProviderRateResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the core model patterns: .from() / .to() / fromPipeFormat() /
 * toPipeFormat()
 */
public class ExchangeRateServiceTest {

    @Test
    void exchangeRateRequest_toPipeFormat_and_back() {
        ExchangeRateRequest original = ExchangeRateRequest.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100.00"))
                .build();

        String pipe = original.toPipeFormat();

        assertEquals("USD|INR|100.00", pipe);

        ExchangeRateRequest restored = ExchangeRateRequest.fromPipeFormat(pipe);

        assertEquals(original.getFromCurrency(), restored.getFromCurrency());
        assertEquals(original.getToCurrency(), restored.getToCurrency());
        assertEquals(original.getAmount(), restored.getAmount());
    }

    @Test
    void providerRateResponse_toPipeFormat_and_back() {
        ProviderRateResponse original = ProviderRateResponse.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .providerReference("REF123")
                .timestamp(1234567890L)
                .build();

        String pipe = original.toPipeFormat();

        assertEquals("USD|INR|83.45|REF123|1234567890", pipe);

        ProviderRateResponse restored = ProviderRateResponse.fromPipeFormat(pipe);

        assertEquals(original.getFromCurrency(), restored.getFromCurrency());
        assertEquals(original.getToCurrency(), restored.getToCurrency());
        assertEquals(original.getRate(), restored.getRate());
        assertEquals(original.getProviderReference(), restored.getProviderReference());
        assertEquals(original.getTimestamp(), restored.getTimestamp());
    }

    @Test
    void exchangeRateResponse_fromProviderResponse() {
        ExchangeRateRequest request = ExchangeRateRequest.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .build();

        ProviderRateResponse providerResponse = ProviderRateResponse.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .providerReference("REF_001")
                .timestamp(System.currentTimeMillis())
                .build();

        ExchangeRateResponse response = ExchangeRateResponse.fromProviderResponse(
                ProviderCodes.EXCHANGE_RATE_API, request, providerResponse);

        assertEquals("USD", response.getFromCurrency());
        assertEquals("INR", response.getToCurrency());
        assertEquals(0, new BigDecimal("83.45").compareTo(response.getRate()));
        assertEquals(0, new BigDecimal("8345.00").compareTo(response.getConvertedAmount()));
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(ProviderCodes.EXCHANGE_RATE_API, response.getProviderCode());
    }

    @Test
    void exchangeRateResponse_failed() {
        ExchangeRateRequest request = ExchangeRateRequest.builder()
                .fromCurrency("USD")
                .toCurrency("XYZ")
                .amount(new BigDecimal("100"))
                .build();

        ExchangeRateResponse response = ExchangeRateResponse.failed(
                ProviderCodes.OPEN_EXCHANGE_RATES, request, "UNSUPPORTED_CURRENCY");

        assertEquals("FAILED_UNSUPPORTED_CURRENCY", response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getRate());
        assertEquals(ProviderCodes.OPEN_EXCHANGE_RATES, response.getProviderCode());
    }

    @Test
    void exchangeRateResponse_toPipeFormat_and_back() {
        ExchangeRateResponse original = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .build();

        String pipe = original.toPipeFormat();
        ExchangeRateResponse restored = ExchangeRateResponse.fromPipeFormat(pipe);

        assertEquals(original.getProviderCode(), restored.getProviderCode());
        assertEquals(original.getFromCurrency(), restored.getFromCurrency());
        assertEquals(original.getToCurrency(), restored.getToCurrency());
        assertEquals(0, original.getRate().compareTo(restored.getRate()));
        assertEquals(0, original.getConvertedAmount().compareTo(restored.getConvertedAmount()));
        assertEquals(original.getStatus(), restored.getStatus());
    }
}
