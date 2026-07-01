package com.example.exchangerate.controllers;

import com.example.exchangerate.config.BatchConfig;
import com.example.exchangerate.models.CurrencyInfo;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.models.ProviderRateDetail;
import com.example.exchangerate.models.RateCompareRequest;
import com.example.exchangerate.models.RateCompareResponse;
import com.example.exchangerate.services.CurrencyCacheService;
import com.example.exchangerate.services.CurrencyMetricsCollector;
import com.example.exchangerate.services.CurrencyService;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateControllerTest {

    private ExchangeRateController controller;
    private ExchangeRateOrchestrationService orchestrationService;
    private CurrencyCacheService currencyCacheService;

    @BeforeEach
    void setUp() {
        orchestrationService = mock(ExchangeRateOrchestrationService.class);
        CurrencyService currencyService = new CurrencyService();
        currencyCacheService = new CurrencyCacheService(currencyService, new CurrencyMetricsCollector());
        BatchConfig batchConfig = new BatchConfig();
        controller = new ExchangeRateController(orchestrationService, currencyCacheService, batchConfig);
    }

    @Test
    void compareRates_returnsResponseForValidCurrencies() {
        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(BigDecimal.ONE)
                .build();

        RateCompareResponse expected = RateCompareResponse.builder()
                .fromCurrency("USD")
                .toCurrency("INR")
                .providerRates(List.of(
                        ProviderRateDetail.builder()
                                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                                .rate(new BigDecimal("83.45"))
                                .status("SUCCESS")
                                .build(),
                        ProviderRateDetail.builder()
                                .providerCode(ProviderCodes.OPEN_EXCHANGE_RATES)
                                .rate(new BigDecimal("83.50"))
                                .status("SUCCESS")
                                .build()
                ))
                .bestRate(new BigDecimal("83.50"))
                .bestProvider(ProviderCodes.OPEN_EXCHANGE_RATES)
                .build();

        when(orchestrationService.compareRates(any())).thenReturn(CompletableFuture.completedFuture(expected));

        RateCompareResponse response = controller.compareRates(request);

        assertEquals("USD", response.getFromCurrency());
        assertEquals("INR", response.getToCurrency());
        assertEquals(2, response.getProviderRates().size());
        assertEquals(new BigDecimal("83.50"), response.getBestRate());
    }

    @Test
    void compareRates_throwsForUnsupportedFromCurrency() {
        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("XYZ")
                .toCurrency("USD")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.compareRates(request));
    }

    @Test
    void compareRates_throwsForUnsupportedToCurrency() {
        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("USD")
                .toCurrency("XYZ")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.compareRates(request));
    }
}
