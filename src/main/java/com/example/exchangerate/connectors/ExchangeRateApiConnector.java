package com.example.exchangerate.connectors;

import com.example.exchangerate.clients.ExchangeRateApiClient;
import com.example.exchangerate.clients.ExchangeRateApiClientConfig;
import com.example.exchangerate.clients.ProviderStaticConfigs;
import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.ProviderCodes;
import com.example.exchangerate.providers.ExchangeRateProvider;
import com.example.exchangerate.providers.ProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.exchangerate.models.ProviderCodes.EXCHANGE_RATE_API;

@Slf4j
@Component
public class ExchangeRateApiConnector extends ExchangeRateProvider {

    private final ProviderFactory factory;
    private final ExchangeRateApiClient client;
    private final Map<String, String> staticConfigs;

    public ExchangeRateApiConnector(ProviderFactory factory,
            ExchangeRateApiClient client,
            ProviderStaticConfigs providerStaticConfigs) {
        this.factory = factory;
        this.client = client;
        this.staticConfigs = providerStaticConfigs.getConfigs().get(EXCHANGE_RATE_API);
    }

    @PostConstruct
    public void init() {
        factory.register(EXCHANGE_RATE_API, this, ExchangeRateApiClientConfig.class);
    }

    @Override
    public ProviderCodes getProviderCode() {
        return EXCHANGE_RATE_API;
    }

    @Override
    protected CompletableFuture<ExchangeRateResponse> doFetchRate(ExchangeRateRequest request) {
        log.info("ExchangeRateApiConnector handling {}->{} | traceId={}",
                request.getFromCurrency(), request.getToCurrency(),
                MDC.get("X-B3-TraceId"));

        ExchangeRateApiClientConfig config = ExchangeRateApiClientConfig.fromStaticConfigs(
                staticConfigs, Map.of(), new ExchangeRateApiClientConfig());

        return client.fetchRate(request, config)
                .thenApply(providerResponse -> ExchangeRateResponse.fromProviderResponse(
                        EXCHANGE_RATE_API, request, providerResponse));
    }
}
