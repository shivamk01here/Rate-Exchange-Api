package com.example.exchangerate.clients;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ProviderRateResponse;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.exchangerate.models.ProviderCodes.EXCHANGE_RATE_API;

@Slf4j
@Component
public class ExchangeRateApiClient {

    private final WebClient client;
    private final Map<String, String> staticConfigs;

    public ExchangeRateApiClient(WebClient.Builder builder,
            ProviderStaticConfigs providerStaticConfigs) {
        this.client = builder
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                        ProviderClientConfig.CONNECT_TIMEOUT_MILLIS)))
                .build();
        this.staticConfigs = providerStaticConfigs.getConfigs().get(EXCHANGE_RATE_API);
    }

    public CompletableFuture<ProviderRateResponse> fetchRate(ExchangeRateRequest request,
            ExchangeRateApiClientConfig config) {
        String pipeRequest = request.toPipeFormat();
        log.info("Sending pipe-delimited request: {}", pipeRequest);

        return client.post()
                .uri(config.getBaseUrl() + "/v1/rates")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .header("X-API-Key", config.getApiKey())
                .bodyValue(pipeRequest)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture()
                .thenApply(responseBody -> {
                    log.info("Received pipe-delimited response: {}", responseBody);
                    return ProviderRateResponse.fromPipeFormat(responseBody);
                });
    }
}
