package com.example.exchangerate.controllers;

import com.example.exchangerate.models.ExchangeRateRequest;
import com.example.exchangerate.models.ExchangeRateResponse;
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
import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateOrchestrationService orchestrationService;
    private final CurrencyCacheService currencyCacheService;

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
