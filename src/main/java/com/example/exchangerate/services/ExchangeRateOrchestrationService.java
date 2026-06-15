package com.example.exchangerate.services;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateOrchestrationService {

    private final ProviderFactory providerFactory;
    private final AuditService auditService;
    private final RateCacheService rateCacheService;

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

        return provider.fetchRate(request)
                .thenCompose(response -> {
                    auditService.recordConversion(response);
                    if ("SUCCESS".equals(response.getStatus())) {
                        log.info("Provider {} succeeded: rate={}", code, response.getRate());
                        rateCacheService.put(response.getFromCurrency(), response.getToCurrency(), response);
                        return CompletableFuture.completedFuture(response);
                    }
                    log.warn("Provider {} failed, trying next", code);
                    return tryProviders(request, index + 1);
                });
    }
}
