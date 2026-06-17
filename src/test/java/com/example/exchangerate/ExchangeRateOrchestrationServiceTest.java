package com.example.exchangerate;

import com.example.exchangerate.models.BatchConversionRequest;
import com.example.exchangerate.models.BatchConversionResponse;
import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import com.example.exchangerate.audit.AuditRepository;
import com.example.exchangerate.config.CacheConfig;
import com.example.exchangerate.services.AuditService;
import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import com.example.exchangerate.services.RateCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateOrchestrationServiceTest {

    private ExchangeRateOrchestrationService orchestrationService;
    private ProviderFactory providerFactory;
    private RateCacheService rateCacheService;
    private AuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        providerFactory = mock(ProviderFactory.class);
        rateCacheService = new RateCacheService(new CacheConfig(), new CacheMetricsCollector());
        auditRepository = new AuditRepository();
        var auditService = new AuditService(auditRepository, null);
        orchestrationService = new ExchangeRateOrchestrationService(providerFactory, auditService, rateCacheService);
    }

    @Test
    void getBatchRates_returnsResultsForAllCurrencies() {
        ExchangeRateProvider mockProvider = mock(ExchangeRateProvider.class);
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(mockProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(mockProvider);

        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();

        when(mockProvider.fetchRate(any(ExchangeRateRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(success));

        BatchConversionRequest batchRequest = BatchConversionRequest.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .toCurrencies(List.of("INR", "EUR"))
                .build();

        BatchConversionResponse response = orchestrationService.getBatchRates(batchRequest).join();

        assertEquals("USD", response.getFromCurrency());
        assertEquals(2, response.getResults().size());
        assertEquals("SUCCESS", response.getResults().get(0).getStatus());
        assertEquals("SUCCESS", response.getResults().get(1).getStatus());
    }

    @Test
    void getBatchRates_handlesPartialFailures() {
        ExchangeRateProvider mockProvider = mock(ExchangeRateProvider.class);
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(mockProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(mockProvider);

        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();

        ExchangeRateResponse failed = ExchangeRateResponse.failed(
                null,
                ExchangeRateRequest.builder().fromCurrency("USD").toCurrency("XYZ").amount(new BigDecimal("100")).build(),
                "UNSUPPORTED_CURRENCY");

        when(mockProvider.fetchRate(any(ExchangeRateRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(success))
                .thenReturn(CompletableFuture.completedFuture(failed));

        BatchConversionRequest batchRequest = BatchConversionRequest.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .toCurrencies(List.of("INR", "EUR"))
                .build();

        BatchConversionResponse response = orchestrationService.getBatchRates(batchRequest).join();

        assertEquals(2, response.getResults().size());
    }

    @Test
    void getBatchRates_withSingleCurrency_returnsSingleResult() {
        ExchangeRateProvider mockProvider = mock(ExchangeRateProvider.class);
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(mockProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(mockProvider);

        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();

        when(mockProvider.fetchRate(any(ExchangeRateRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(success));

        BatchConversionRequest batchRequest = BatchConversionRequest.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .toCurrencies(List.of("INR"))
                .build();

        BatchConversionResponse response = orchestrationService.getBatchRates(batchRequest).join();

        assertEquals(1, response.getResults().size());
        assertEquals("INR", response.getResults().get(0).getToCurrency());
    }
}
