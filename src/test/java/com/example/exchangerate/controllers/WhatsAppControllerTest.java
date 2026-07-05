package com.example.exchangerate.controllers;

import com.example.exchangerate.whatsapp.WhatsAppProviderType;
import com.example.exchangerate.whatsapp.WhatsAppRequest;
import com.example.exchangerate.whatsapp.WhatsAppResponse;
import com.example.exchangerate.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(WhatsAppController.class)
class WhatsAppControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WhatsAppService whatsAppService;

    @Test
    void shouldSendWhatsApp() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp")
                .build();

        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(true)
                .messageId("MSG123")
                .provider(WhatsAppProviderType.CONSOLE)
                .build();

        when(whatsAppService.send(any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));

        webTestClient.post().uri("/api/whatsapp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.messageId").isEqualTo("MSG123");
    }

    @Test
    void shouldReturn503OnFailure() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp")
                .build();

        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(false)
                .errorMessage("Provider unavailable")
                .build();

        when(whatsAppService.send(any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));

        webTestClient.post().uri("/api/whatsapp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(503);
    }

    @Test
    void shouldSendWhatsAppWithProvider() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp via Twilio")
                .build();

        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(true)
                .messageId("WATW123")
                .provider(WhatsAppProviderType.TWILIO)
                .build();

        when(whatsAppService.sendWithProvider(any(), any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));

        webTestClient.post().uri("/api/whatsapp/send/TWILIO")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.provider").isEqualTo("TWILIO");
    }

    @Test
    void shouldReturnBadRequestForInvalidProvider() {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        webTestClient.post().uri("/api/whatsapp/send/INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
