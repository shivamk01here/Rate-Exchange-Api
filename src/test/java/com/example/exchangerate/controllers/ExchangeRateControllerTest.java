package com.example.exchangerate.controllers;

import com.example.exchangerate.config.BatchConfig;
import com.example.exchangerate.config.ConversionHistoryConfig;
import com.example.exchangerate.history.ConversionHistoryRepository;
import com.example.exchangerate.history.ConversionHistoryService;
import com.example.exchangerate.models.ConversionQueryParams;
import com.example.exchangerate.models.CurrencyInfo;
import com.example.exchangerate.models.ExchangeRateResponse;
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
        ConversionHistoryConfig historyConfig = new ConversionHistoryConfig();
        ConversionHistoryService historyService = new ConversionHistoryService(new ConversionHistoryRepository(), historyConfig);
        controller = new ExchangeRateController(orchestrationService, currencyCacheService, batchConfig, historyService, historyConfig);
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

        RateCompareResponse response = controller.compareRates(request).join();

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

        assertThrows(ResponseStatusException.class, () -> controller.compareRates(request).join());
    }

    @Test
    void compareRates_throwsForUnsupportedToCurrency() {
        RateCompareRequest request = RateCompareRequest.builder()
                .fromCurrency("USD")
                .toCurrency("XYZ")
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.compareRates(request).join());
    }

    @Test
    void convertViaGet_returnsResponseForValidParams() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("INR")
                .amount(new BigDecimal("100"))
                .build();

        ExchangeRateResponse expected = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("INR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("83.45"))
                .convertedAmount(new BigDecimal("8345.00"))
                .status("SUCCESS")
                .build();

        when(orchestrationService.getRate(any())).thenReturn(CompletableFuture.completedFuture(expected));

        ExchangeRateResponse response = controller.convertViaGet(params).join();

        assertEquals("USD", response.getFromCurrency());
        assertEquals("INR", response.getToCurrency());
        assertEquals(new BigDecimal("83.45"), response.getRate());
        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void convertViaGet_throwsForUnsupportedFromCurrency() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("XYZ")
                .to("USD")
                .amount(new BigDecimal("100"))
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.convertViaGet(params).join());
    }

    @Test
    void convertViaGet_throwsForUnsupportedToCurrency() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("XYZ")
                .amount(new BigDecimal("100"))
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.convertViaGet(params).join());
    }

    @Test
    void convertViaGet_throwsForAmountExceedingMax() {
        ConversionQueryParams params = ConversionQueryParams.builder()
                .from("USD")
                .to("EUR")
                .amount(new BigDecimal("999999999"))
                .build();

        assertThrows(ResponseStatusException.class, () -> controller.convertViaGet(params).join());
    }
}
