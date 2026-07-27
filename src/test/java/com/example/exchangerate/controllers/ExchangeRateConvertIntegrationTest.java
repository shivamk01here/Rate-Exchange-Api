package com.example.exchangerate.controllers;

import com.example.exchangerate.config.BatchConfig;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.services.CurrencyCacheService;
import com.example.exchangerate.services.CurrencyMetricsCollector;
import com.example.exchangerate.services.CurrencyService;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ExchangeRateController.class)
class ExchangeRateConvertIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ExchangeRateOrchestrationService orchestrationService;

    @MockBean
    private CurrencyCacheService currencyCacheService;

    @MockBean
    private BatchConfig batchConfig;

    @Test
    void getConvert_returnsRateForValidParams() {
        ExchangeRateResponse response = ExchangeRateResponse.builder()
                .providerCode(ProviderCodes.EXCHANGE_RATE_API)
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .rate(new BigDecimal("0.92"))
                .convertedAmount(new BigDecimal("92.00"))
                .status("SUCCESS")
                .build();

        when(currencyCacheService.isSupported("USD")).thenReturn(true);
        when(currencyCacheService.isSupported("EUR")).thenReturn(true);
        when(batchConfig.getMaxAmount()).thenReturn(1000000);
        when(orchestrationService.getRate(any())).thenReturn(CompletableFuture.completedFuture(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/rates/convert")
                        .queryParam("from", "USD")
                        .queryParam("to", "EUR")
                        .queryParam("amount", "100")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.fromCurrency").isEqualTo("USD")
                .jsonPath("$.toCurrency").isEqualTo("EUR")
                .jsonPath("$.rate").isEqualTo(0.92)
                .jsonPath("$.status").isEqualTo("SUCCESS");
    }

    @Test
    void getConvert_returns400ForUnsupportedCurrency() {
        when(currencyCacheService.isSupported("XYZ")).thenReturn(false);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/rates/convert")
                        .queryParam("from", "XYZ")
                        .queryParam("to", "EUR")
                        .queryParam("amount", "100")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getConvert_returns503WhenAllProvidersFail() {
        ExchangeRateResponse failedResponse = ExchangeRateResponse.builder()
                .fromCurrency("USD")
                .toCurrency("EUR")
                .amount(new BigDecimal("100"))
                .rate(BigDecimal.ZERO)
                .convertedAmount(BigDecimal.ZERO)
                .status("FAILED_ALL_PROVIDERS_FAILED")
                .build();

        when(currencyCacheService.isSupported("USD")).thenReturn(true);
        when(currencyCacheService.isSupported("EUR")).thenReturn(true);
        when(batchConfig.getMaxAmount()).thenReturn(1000000);
        when(orchestrationService.getRate(any())).thenReturn(CompletableFuture.completedFuture(failedResponse));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/rates/convert")
                        .queryParam("from", "USD")
                        .queryParam("to", "EUR")
                        .queryParam("amount", "100")
                        .build())
                .exchange()
                .expectStatus().isEqualTo(503);
    }
}
