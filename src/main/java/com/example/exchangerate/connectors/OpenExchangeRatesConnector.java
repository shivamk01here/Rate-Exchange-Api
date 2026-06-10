package com.example.exchangerate.connectors;

import com.example.exchangerate.clients.OpenExchangeRatesClientConfig;
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
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.exchangerate.models.ProviderCodes.OPEN_EXCHANGE_RATES;

@Slf4j
@Component
public class OpenExchangeRatesConnector extends ExchangeRateProvider {

    private final ProviderFactory factory;
    private final Map<String, String> staticConfigs;

    public OpenExchangeRatesConnector(ProviderFactory factory,
            ProviderStaticConfigs providerStaticConfigs) {
        this.factory = factory;
        this.staticConfigs = providerStaticConfigs.getConfigs().get(OPEN_EXCHANGE_RATES);
    }

    @PostConstruct
    public void init() {
        factory.register(OPEN_EXCHANGE_RATES, this, OpenExchangeRatesClientConfig.class);
    }

    @Override
    public ProviderCodes getProviderCode() {
        return OPEN_EXCHANGE_RATES;
    }

    @Override
    protected CompletableFuture<ExchangeRateResponse> doFetchRate(ExchangeRateRequest request) {
        log.info("OpenExchangeRatesConnector handling {}->{} | traceId={}",
                request.getFromCurrency(), request.getToCurrency(),
                MDC.get("X-B3-TraceId"));

        OpenExchangeRatesClientConfig config = OpenExchangeRatesClientConfig.fromStaticConfigs(
                staticConfigs, Map.of(), new OpenExchangeRatesClientConfig());

        return CompletableFuture.supplyAsync(() -> {
            BigDecimal rate = fetchFromOpenExchangeRates(config, request);
            return ExchangeRateResponse.builder()
                    .providerCode(OPEN_EXCHANGE_RATES)
                    .fromCurrency(request.getFromCurrency())
                    .toCurrency(request.getToCurrency())
                    .amount(request.getAmount())
                    .rate(rate)
                    .convertedAmount(request.getAmount().multiply(rate))
                    .status("SUCCESS")
                    .build();
        });
    }

    private BigDecimal fetchFromOpenExchangeRates(OpenExchangeRatesClientConfig config,
            ExchangeRateRequest request) {
        log.info("Simulating OpenExchangeRates API call to {} with appId={}",
                config.getBaseUrl(), config.getAppId());
        return new BigDecimal("83.45");
    }
}
