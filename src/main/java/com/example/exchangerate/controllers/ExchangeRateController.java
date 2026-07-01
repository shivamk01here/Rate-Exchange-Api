package com.example.exchangerate.controllers;

import com.example.exchangerate.config.BatchConfig;
import com.example.exchangerate.models.BatchConversionRequest;
import com.example.exchangerate.models.BatchConversionResponse;
import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
import com.example.exchangerate.models.RateCompareRequest;
import com.example.exchangerate.models.RateCompareResponse;
import com.example.exchangerate.services.CurrencyCacheService;
import com.example.exchangerate.services.ExchangeRateOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateOrchestrationService orchestrationService;
    private final CurrencyCacheService currencyCacheService;
    private final BatchConfig batchConfig;

    @PostMapping(value = "/rates", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ExchangeRateResponse> getRate(
            @Valid @RequestBody ExchangeRateRequest request,
            ServerWebExchange exchange) {

        String from = request.getFromCurrency();
        String to = request.getToCurrency();

        if (!currencyCacheService.isSupported(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + from);
        }
        if (!currencyCacheService.isSupported(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + to);
        }
        if (request.getAmount().compareTo(BigDecimal.valueOf(batchConfig.getMaxAmount())) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount exceeds maximum of " + batchConfig.getMaxAmount());
        }

        String traceId = MDC.get("X-B3-TraceId");
        log.info("Received rate request: {}->{} amount={} | traceId={}",
                from, to,
                request.getAmount(), traceId);

        return orchestrationService.getRate(request)
                .thenApply(response -> {
                    if (response.getStatus().startsWith("FAILED")) {
                        throw new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No provider could fulfill the request: " + response.getStatus());
                    }
                    return response;
                });
    }

    @PostMapping(value = "/rates/batch")
    public CompletableFuture<BatchConversionResponse> getBatchRates(
            @Valid @RequestBody BatchConversionRequest batchRequest) {

        String from = batchRequest.getFromCurrency();
        if (!currencyCacheService.isSupported(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + from);
        }

        if (batchRequest.getToCurrencies().size() > batchConfig.getMaxSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Batch size exceeds maximum of " + batchConfig.getMaxSize());
        }
        if (batchRequest.getAmount().compareTo(BigDecimal.valueOf(batchConfig.getMaxAmount())) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount exceeds maximum of " + batchConfig.getMaxAmount());
        }

        String unsupported = batchRequest.getToCurrencies().stream()
                .filter(c -> !currencyCacheService.isSupported(c))
                .collect(Collectors.joining(", "));
        if (!unsupported.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported currencies: " + unsupported);
        }

        log.info("Received batch rate request: {} {} -> {}",
                batchRequest.getAmount(), from, batchRequest.getToCurrencies());

        return orchestrationService.getBatchRates(batchRequest);
    }

    @PostMapping(value = "/rates/compare")
    public CompletableFuture<RateCompareResponse> compareRates(
            @Valid @RequestBody RateCompareRequest request) {

        String from = request.getFromCurrency();
        String to = request.getToCurrency();

        if (!currencyCacheService.isSupported(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + from);
        }
        if (!currencyCacheService.isSupported(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + to);
        }

        log.info("Received rate comparison request: {}->{}", from, to);
        return orchestrationService.compareRates(request);
    }

    @PostMapping(value = "/rates/pipe", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Mono<Void> getRateViaPipe(
            @RequestBody String pipeRequest,
            ServerWebExchange exchange) {

        ExchangeRateRequest request = ExchangeRateRequest.fromPipeFormat(pipeRequest);

        if (!currencyCacheService.isSupported(request.getFromCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported currency: " + request.getFromCurrency());
        }
        if (!currencyCacheService.isSupported(request.getToCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported currency: " + request.getToCurrency());
        }

        return Mono.fromFuture(
                orchestrationService.getRate(request)).flatMap(response -> {
                    ServerHttpResponse httpResponse = exchange.getResponse();
                    httpResponse.setStatusCode(HttpStatus.FOUND);
                    httpResponse.getHeaders().setLocation(
                            URI.create("/api/rates/result?data=" + response.toPipeFormat()));
                    return httpResponse.setComplete();
                });
    }
}
