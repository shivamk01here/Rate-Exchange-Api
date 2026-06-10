package com.example.exchangerate.providers;

import com.example.exchangerate.clients.ProviderClientConfig;
import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class ExchangeRateProvider {

    public abstract ProviderCodes getProviderCode();

    protected abstract CompletableFuture<ExchangeRateResponse> doFetchRate(ExchangeRateRequest request);

    public final CompletableFuture<ExchangeRateResponse> fetchRate(ExchangeRateRequest request) {
        log.info("Provider {} fetching rate for {}->{} | traceId={}",
                getProviderCode(), request.getFromCurrency(), request.getToCurrency(),
                MDC.get("X-B3-TraceId"));

        return doFetchRate(request)
                .exceptionally(throwable -> {
                    log.error("Provider {} failed: {}", getProviderCode(), throwable.getMessage());
                    return ExchangeRateResponse.failed(
                            getProviderCode(), request, throwable.getMessage());
                });
    }

    public interface Factory {
        void register(ProviderCodes code, ExchangeRateProvider provider,
                Class<? extends ProviderClientConfig> configClass);

        ExchangeRateProvider getProvider(ProviderCodes code);

        Class<? extends ProviderClientConfig> getConfigClass(ProviderCodes code);
    }
}