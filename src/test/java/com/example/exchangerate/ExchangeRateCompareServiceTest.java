package com.example.exchangerate;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.models.ProviderRateDetail;
import com.example.exchangerate.models.RateCompareRequest;
import com.example.exchangerate.models.RateCompareResponse;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import com.example.exchangerate.audit.AuditRepository;
import com.example.exchangerate.config.CacheConfig;
import com.example.exchangerate.services.AuditService;
import com.example.exchangerate.services.CacheMetricsCollector;
import com.example.exchangerate.alert.AlertEvaluationService;
import com.example.exchangerate.alert.AlertRepository;
import com.example.exchangerate.notification.EmailConfig;
import com.example.exchangerate.notification.EmailService;
import com.example.exchangerate.whatsapp.WhatsAppAlertService;
import com.example.exchangerate.whatsapp.WhatsAppConfig;
import com.example.exchangerate.whatsapp.WhatsAppService;
import com.example.exchangerate.whatsapp.WhatsAppProviderFactory;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import com.example.exchangerate.services.ProviderMetricsCollector;
import com.example.exchangerate.services.RateCacheService;
import com.example.exchangerate.trend.RateTrendConfig;
import com.example.exchangerate.trend.RateTrendMetricsCollector;
import com.example.exchangerate.trend.RateTrendRepository;
import com.example.exchangerate.trend.RateTrendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateCompareServiceTest {

    private ExchangeRateOrchestrationService orchestrationService;
    private ProviderFactory providerFactory;
    private ProviderMetricsCollector providerMetrics;

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
        providerMetrics = new ProviderMetricsCollector();
        var rateCacheService = new RateCacheService(new CacheConfig(), new CacheMetricsCollector());
        var auditRepository = new AuditRepository();
        var auditService = new AuditService(auditRepository, null);
        var waFactory = new WhatsAppProviderFactory();
        var waConfig = new WhatsAppConfig();
        var waService = new WhatsAppService(waFactory, waConfig);
        var waAlertService = new WhatsAppAlertService(waService, waConfig);
        var alertEvalService = new AlertEvaluationService(
                new AlertRepository(), providerFactory,
                new EmailService(null, new EmailConfig()), waAlertService);
        var rateTrendConfig = new RateTrendConfig();
        var rateTrendService = new RateTrendService(
                new RateTrendRepository(), rateTrendConfig, new RateTrendMetricsCollector());
        orchestrationService = new ExchangeRateOrchestrationService(
                providerFactory, auditService, rateCacheService, providerMetrics,
                alertEvalService, rateTrendService, rateTrendConfig);
    }

    @Test
    void compareRates_returnsRatesFromAllProviders() {
        ExchangeRateResponse successEra = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("83.45"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();
        ExchangeRateResponse successOer = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("INR")
                .rate(new BigDecimal("83.50"))
                .convertedAmount(new BigDecimal("83.50"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.OPEN_EXCHANGE_RATES)
                .build();

        ExchangeRateProvider eraProvider = new StubProvider(
                ProviderCodes.EXCHANGE_RATE_API, CompletableFuture.completedFuture(successEra));
        ExchangeRateProvider oerProvider = new StubProvider(
                ProviderCodes.OPEN_EXCHANGE_RATES, CompletableFuture.completedFuture(successOer));
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(eraProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(oerProvider);

        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(BigDecimal.ONE)
                .build();

        RateCompareResponse response = orchestrationService.compareRates(request).join();

        assertEquals("USD", response.getFromCurrency());
        assertEquals("INR", response.getToCurrency());
        assertEquals(2, response.getProviderRates().size());
        assertEquals(new BigDecimal("83.50"), response.getBestRate());
        assertEquals(ProviderCodes.OPEN_EXCHANGE_RATES, response.getBestProvider());
    }

    @Test
    void compareRates_handlesProviderFailure() {
        ExchangeRateResponse success = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("EUR")
                .rate(new BigDecimal("0.92"))
                .convertedAmount(new BigDecimal("0.92"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();
        ExchangeRateResponse failed = ExchangeRateResponse.failed(
                ProviderCodes.OPEN_EXCHANGE_RATES,
                ExchangeRateRequest.builder().fromCurrency("USD").toCurrency("EUR").amount(BigDecimal.ONE).build(),
                "SERVICE_DOWN");

        ExchangeRateProvider eraProvider = new StubProvider(
                ProviderCodes.EXCHANGE_RATE_API, CompletableFuture.completedFuture(success));
        ExchangeRateProvider oerProvider = new StubProvider(
                ProviderCodes.OPEN_EXCHANGE_RATES, CompletableFuture.completedFuture(failed));
        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API)).thenReturn(eraProvider);
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES)).thenReturn(oerProvider);

        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .build();

        RateCompareResponse response = orchestrationService.compareRates(request).join();

        assertEquals(2, response.getProviderRates().size());
        assertEquals("SUCCESS", response.getProviderRates().get(0).getStatus());
        assertTrue(response.getProviderRates().get(1).getStatus().startsWith("FAILED"));
        assertEquals(new BigDecimal("0.92"), response.getBestRate());
    }

    @Test
    void compareRates_returnsBestRateWhenAllSucceed() {
        ExchangeRateResponse low = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .rate(new BigDecimal("0.75"))
                .convertedAmount(new BigDecimal("0.75"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .build();
        ExchangeRateResponse high = ExchangeRateResponse.builder()
                .fromCurrency("USD").toCurrency("GBP")
                .rate(new BigDecimal("0.78"))
                .convertedAmount(new BigDecimal("0.78"))
                .status("SUCCESS")
                .providerCode(ProviderCodes.OPEN_EXCHANGE_RATES)
                .build();

        when(providerFactory.getProvider(ProviderCodes.EXCHANGE_RATE_API))
                .thenReturn(new StubProvider(ProviderCodes.EXCHANGE_RATE_API, CompletableFuture.completedFuture(low)));
        when(providerFactory.getProvider(ProviderCodes.OPEN_EXCHANGE_RATES))
                .thenReturn(new StubProvider(ProviderCodes.OPEN_EXCHANGE_RATES, CompletableFuture.completedFuture(high)));

        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("USD").toCurrency("GBP").build();

        RateCompareResponse response = orchestrationService.compareRates(request).join();

        assertEquals(new BigDecimal("0.78"), response.getBestRate());
        assertEquals(ProviderCodes.OPEN_EXCHANGE_RATES, response.getBestProvider());
    }
}
