package com.example.exchangerate.controllers;

import com.example.exchangerate.whatsapp.WhatsAppProviderType;
import com.example.exchangerate.whatsapp.WhatsAppRequest;
import com.example.exchangerate.whatsapp.WhatsAppResponse;
import com.example.exchangerate.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<WhatsAppResponse>> sendWhatsApp(@Valid @RequestBody WhatsAppRequest request) {
        log.info("REST request to send WhatsApp to {}", request.getTo());
        return whatsAppService.send(request)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.status(503).body(response);
                });
    }

    @PostMapping("/send/{provider}")
    public CompletableFuture<ResponseEntity<WhatsAppResponse>> sendWhatsAppWithProvider(
            @PathVariable String provider,
            @Valid @RequestBody WhatsAppRequest request) {
        WhatsAppProviderType providerType;
        try {
            providerType = WhatsAppProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid WhatsApp provider requested: {}", provider);
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().build());
        }

        log.info("REST request to send WhatsApp via {} to {}", providerType, request.getTo());
        return whatsAppService.sendWithProvider(request, providerType)
                .thenApply(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    }
                    return ResponseEntity.status(503).body(response);
                });
    }
}
