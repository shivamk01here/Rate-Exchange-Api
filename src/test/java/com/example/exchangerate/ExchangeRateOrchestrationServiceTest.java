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
import com.example.exchangerate.alert.AlertEvaluationService;
import com.example.exchangerate.alert.AlertRepository;
import com.example.exchangerate.notification.EmailConfig;
import com.example.exchangerate.notification.EmailService;
import com.example.exchangerate.whatsapp.WhatsAppAlertService;
import com.example.exchangerate.whatsapp.WhatsAppConfig;
import com.example.exchangerate.whatsapp.WhatsAppService;
import com.example.exchangerate.whatsapp.WhatsAppProviderFactory;
import com.example.exchangerate.services.AuditService;
import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import com.example.exchangerate.services.ProviderMetricsCollector;
import com.example.exchangerate.services.RateCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateOrchestrationServiceTest {

    private ExchangeRateOrchestrationService orchestrationService;
    private ProviderFactory providerFactory;
    private RateCacheService rateCacheService;
    private AuditRepository auditRepository;

    private static class StubProvider extends ExchangeRateProvider {
        private final ProviderCodes code;
        private final CompletableFuture<ExchangeRateResponse> response;

        StubProvider(ProviderCodes code, CompletableFuture<ExchangeRateResponse> response) {
            this.code = code;
            this.response = response;
        }

        @Override
        public ProviderCodes getProviderCode() {
            return code;
        }

        @Override
        protected CompletableFuture<ExchangeRateResponse> doFetchRate(ExchangeRateRequest request) {
            return response;
        }
    }

    @BeforeEach
    void setUp() {
        providerFactory = mock(ProviderFactory.class);
        rateCacheService = new RateCacheService(new CacheConfig(), new CacheMetricsCollector());
        auditRepository = new AuditRepository();
        var auditService = new AuditService(auditRepository, null);
        var waFactory = new WhatsAppProviderFactory();
        var waConfig = new WhatsAppConfig();
        var waService = new WhatsAppService(waFactory, waConfig);
        var waAlertService = new WhatsAppAlertService(waService, waConfig);
        var alertEvalService = new AlertEvaluationService(
                new AlertRepository(), providerFactory,
                new EmailService(null, new EmailConfig()), waAlertService);
        orchestrationService = new ExchangeRateOrchestrationService(
                providerFactory, auditService, rateCacheService, new ProviderMetricsCollector(), alertEvalService);
    }

    @Test
    void getBatchRates_returnsResultsForAllCurrencies() {
        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();

        ExchangeRateProvider stubProvider = new StubProvider(
                ProviderCodes.EXCHANGE_RATE_API, CompletableFuture.completedFuture(success));
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(stubProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(stubProvider);

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
        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();

        ExchangeRateResponse failed = ExchangeRateResponse.failed(
                ProviderCodes.EXCHANGE_RATE_API,
                ExchangeRateRequest.builder().fromCurrency("USD").toCurrency("XYZ").amount(new BigDecimal("100")).build(),
                "UNSUPPORTED_CURRENCY");

        ExchangeRateProvider stubProvider = new StubProvider(
                ProviderCodes.EXCHANGE_RATE_API, CompletableFuture.completedFuture(success));
        ExchangeRateProvider failProvider = new StubProvider(
                ProviderCodes.OPEN_EXCHANGE_RATES, CompletableFuture.completedFuture(failed));
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(stubProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(failProvider);

        BatchConversionRequest batchRequest = BatchConversionRequest.builder()
                .fromCurrency("USD")
                .amount(new BigDecimal("100"))
                .toCurrencies(List.of("INR", "EUR"))
                .build();

        BatchConversionResponse response = orchestrationService.getBatchRates(batchRequest).join();

        assertEquals(2, response.getResults().size());
        assertEquals("SUCCESS", response.getResults().get(0).getStatus());
    }

    @Test
    void getBatchRates_withSingleCurrency_returnsSingleResult() {
        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();

        ExchangeRateProvider stubProvider = new StubProvider(
                ProviderCodes.EXCHANGE_RATE_API, CompletableFuture.completedFuture(success));
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(stubProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(stubProvider);

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
