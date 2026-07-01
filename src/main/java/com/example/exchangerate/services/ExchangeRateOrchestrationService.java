package com.example.exchangerate.services;

import com.example.exchangerate.models.BatchConversionRequest;
import com.example.exchangerate.models.BatchConversionResponse;
import com.example.exchangerate.models.BatchRateResult;
import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.models.ProviderRateDetail;
import com.example.exchangerate.models.RateCompareRequest;
import com.example.exchangerate.models.RateCompareResponse;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateOrchestrationService {

    private final ProviderFactory providerFactory;
    private final AuditService auditService;
    private final RateCacheService rateCacheService;
    private final ProviderMetricsCollector providerMetrics;

    private final ConcurrentHashMap<String, CachedCompare> compareCache = new ConcurrentHashMap<>();
    private static final long COMPARE_CACHE_TTL_SECONDS = 120;

    private record CachedCompare(RateCompareResponse response, Instant expiresAt) {
        boolean isValid() { return Instant.now().isBefore(expiresAt); }
    }

    private static final List<ProviderCodes> ROUTING_ORDER = List.of(
            ProviderCodes.EXCHANGE_RATE_API,
            ProviderCodes.OPEN_EXCHANGE_RATES);

    public CompletableFuture<ExchangeRateResponse> getRate(ExchangeRateRequest request) {
        log.info("Orchestrating rate request {}->{} amount={} | traceId={}",
                request.getFromCurrency(), request.getToCurrency(),
                request.getAmount(), MDC.get("X-B3-TraceId"));

        ExchangeRateResponse cached = rateCacheService.get(
                request.getFromCurrency(), request.getToCurrency());
        if (cached != null) {
            log.info("Returning cached rate for {}->{}: rate={}",
                    request.getFromCurrency(), request.getToCurrency(), cached.getRate());
            return CompletableFuture.completedFuture(cached);
        }

        return tryProviders(request, 0);
    }

    public CompletableFuture<BatchConversionResponse> getBatchRates(BatchConversionRequest batchRequest) {
        log.info("Processing batch conversion: {} {} -> {} currencies",
                batchRequest.getAmount(), batchRequest.getFromCurrency(),
                batchRequest.getToCurrencies().size());

        List<CompletableFuture<BatchRateResult>> futures = batchRequest.getToCurrencies().stream()
                .map(toCurrency -> {
                    ExchangeRateRequest singleRequest = ExchangeRateRequest.builder()
                            .fromCurrency(batchRequest.getFromCurrency())
                            .toCurrency(toCurrency)
                            .amount(batchRequest.getAmount())
                            .build();
                    return getRate(singleRequest)
                            .thenApply(response -> BatchRateResult.builder()
                                    .toCurrency(toCurrency)
                                    .rate(response.getRate())
                                    .convertedAmount(response.getConvertedAmount())
                                    .status(response.getStatus())
                                    .build())
                            .exceptionally(e -> {
                                log.warn("Batch conversion failed for {}->{}: {}",
                                        batchRequest.getFromCurrency(), toCurrency, e.getMessage());
                                return BatchRateResult.builder()
                                        .toCurrency(toCurrency)
                                        .status("FAILED_PROVIDER_ERROR")
                                        .build();
                            });
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<BatchRateResult> results = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                    return BatchConversionResponse.builder()
                            .fromCurrency(batchRequest.getFromCurrency())
                            .amount(batchRequest.getAmount())
                            .results(results)
                            .build();
                });
    }

    public CompletableFuture<RateCompareResponse> compareRates(RateCompareRequest request) {
        log.info("Comparing rates across all providers for {}->{}", request.getFromCurrency(), request.getToCurrency());

        String cacheKey = RateCacheService.cacheKey(request.getFromCurrency(), request.getToCurrency());
        CachedCompare cached = compareCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            log.info("Returning cached comparison for {}", cacheKey);
            return CompletableFuture.completedFuture(cached.response());
        }

        List<CompletableFuture<ProviderRateDetail>> futures = ROUTING_ORDER.stream()
                .map(code -> {
                    ExchangeRateProvider provider = providerFactory.getProvider(code);
                    ExchangeRateRequest rateRequest = ExchangeRateRequest.builder()
                            .fromCurrency(request.getFromCurrency())
                            .toCurrency(request.getToCurrency())
                            .amount(request.getAmount() != null ? request.getAmount() : java.math.BigDecimal.ONE)
                            .build();
                    providerMetrics.recordCall(code);
                    return provider.fetchRate(rateRequest)
                            .thenApply(response -> {
                                if ("SUCCESS".equals(response.getStatus())) {
                                    providerMetrics.recordSuccess(code);
                                } else {
                                    providerMetrics.recordFailure(code);
                                }
                                return ProviderRateDetail.builder()
                                        .providerCode(code)
                                        .rate(response.getRate())
                                        .status(response.getStatus())
                                        .build();
                            })
                            .exceptionally(e -> {
                                providerMetrics.recordFailure(code);
                                log.warn("Provider {} failed during comparison: {}", code, e.getMessage());
                                return ProviderRateDetail.builder()
                                        .providerCode(code)
                                        .rate(java.math.BigDecimal.ZERO)
                                        .status("FAILED_PROVIDER_ERROR")
                                        .build();
                            });
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<ProviderRateDetail> details = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                    RateCompareResponse response = RateCompareResponse.builder()
                            .fromCurrency(request.getFromCurrency())
                            .toCurrency(request.getToCurrency())
                            .providerRates(details)
                            .build();
                    response.computeBest();
                    compareCache.put(cacheKey, new CachedCompare(response, Instant.now().plusSeconds(COMPARE_CACHE_TTL_SECONDS)));
                    auditService.recordConversion(response);
                    return response;
                });
    }

    private CompletableFuture<ExchangeRateResponse> tryProviders(ExchangeRateRequest request,
            int index) {
        if (index >= ROUTING_ORDER.size()) {
            ExchangeRateResponse failed = ExchangeRateResponse.failed(null, request, "ALL_PROVIDERS_FAILED");
            auditService.recordConversion(failed);
            return CompletableFuture.completedFuture(failed);
        }

        ProviderCodes code = ROUTING_ORDER.get(index);
        ExchangeRateProvider provider = providerFactory.getProvider(code);

        log.info("Attempting provider {} (attempt {}/{})", code, index + 1, ROUTING_ORDER.size());
        providerMetrics.recordCall(code);

        return provider.fetchRate(request)
                .thenCompose(response -> {
                    auditService.recordConversion(response);
                    if ("SUCCESS".equals(response.getStatus())) {
                        providerMetrics.recordSuccess(code);
                        log.info("Provider {} succeeded: rate={}", code, response.getRate());
                        rateCacheService.put(response.getFromCurrency(), response.getToCurrency(), response);
                        return CompletableFuture.completedFuture(response);
                    }
                    providerMetrics.recordFailure(code);
                    log.warn("Provider {} failed, trying next", code);
                    return tryProviders(request, index + 1);
                });
    }
}
