package com.example.exchangerate.controllers;

import com.example.exchangerate.whatsapp.WhatsAppProviderType;
import com.example.exchangerate.whatsapp.WhatsAppRequest;
import com.example.exchangerate.whatsapp.WhatsAppResponse;
import com.example.exchangerate.whatsapp.WhatsAppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WhatsAppController.class)
class WhatsAppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WhatsAppService whatsAppService;

    @Test
    void shouldSendWhatsApp() throws Exception {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp")
                .build();

        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(true)
                .messageId("MSG123")
                .provider(WhatsAppProviderType.CONSOLE)
                .build();

        when(whatsAppService.send(any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/whatsapp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value("MSG123"));
    }

    @Test
    void shouldReturn503OnFailure() throws Exception {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp")
                .build();

        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(false)
                .errorMessage("Provider unavailable")
                .build();

        when(whatsAppService.send(any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/whatsapp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldSendWhatsAppWithProvider() throws Exception {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test WhatsApp via Twilio")
                .build();

        WhatsAppResponse response = WhatsAppResponse.builder()
                .success(true)
                .messageId("WATW123")
                .provider(WhatsAppProviderType.TWILIO)
                .build();

        when(whatsAppService.sendWithProvider(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/whatsapp/send/TWILIO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("TWILIO"));
    }

    @Test
    void shouldReturnBadRequestForInvalidProvider() throws Exception {
        WhatsAppRequest request = WhatsAppRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        mockMvc.perform(post("/api/whatsapp/send/INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
