package com.example.exchangerate.controllers;

import com.example.exchangerate.models.ExchangeRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/rates")
public class ExchangeRateRedirectController {

    @GetMapping(value = "/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ExchangeRateResponse> handleRedirect(@RequestParam("data") String pipeData) {
        log.info("Received redirect with pipe data: {}", pipeData);

        ExchangeRateResponse response = ExchangeRateResponse.fromPipeFormat(pipeData);
        return Mono.just(response);
    }
}