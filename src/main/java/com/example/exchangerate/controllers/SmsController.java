package com.example.exchangerate.controllers;

import com.example.exchangerate.sms.SmsProviderType;
import com.example.exchangerate.sms.SmsRequest;
import com.example.exchangerate.sms.SmsResponse;
import com.example.exchangerate.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<SmsResponse>> sendSms(@Valid @RequestBody SmsRequest request) {
        log.info("REST request to send SMS to {}", request.getTo());
        return smsService.send(request)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.status(503).body(response);
                });
    }

    @PostMapping("/send/{provider}")
    public CompletableFuture<ResponseEntity<SmsResponse>> sendSmsWithProvider(
            @PathVariable String provider,
            @Valid @RequestBody SmsRequest request) {
        SmsProviderType providerType;
        try {
            providerType = SmsProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid SMS provider requested: {}", provider);
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build());
        }

        log.info("REST request to send SMS via {} to {}", providerType, request.getTo());
        return smsService.sendWithProvider(request, providerType)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.status(503).body(response);
                });
    }
}
