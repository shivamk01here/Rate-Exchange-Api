package com.example.exchangerate.controllers;

import com.example.exchangerate.sms.SmsProviderType;
import com.example.exchangerate.sms.SmsRequest;
import com.example.exchangerate.sms.SmsResponse;
import com.example.exchangerate.sms.SmsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmsController.class)
class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmsService smsService;

    @Test
    void shouldSendSms() throws Exception {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test SMS")
                .build();

        SmsResponse response = SmsResponse.builder()
                .success(true)
                .messageId("MSG123")
                .provider(SmsProviderType.CONSOLE)
                .build();

        when(smsService.send(any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value("MSG123"));
    }

    @Test
    void shouldReturn503OnFailure() throws Exception {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test SMS")
                .build();

        SmsResponse response = SmsResponse.builder()
                .success(false)
                .errorMessage("Provider unavailable")
                .build();

        when(smsService.send(any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldSendSmsWithProvider() throws Exception {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test SMS via Twilio")
                .build();

        SmsResponse response = SmsResponse.builder()
                .success(true)
                .messageId("TW123")
                .provider(SmsProviderType.TWILIO)
                .build();

        when(smsService.sendWithProvider(any(), any())).thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/sms/send/TWILIO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("TWILIO"));
    }

    @Test
    void shouldReturnBadRequestForInvalidProvider() throws Exception {
        SmsRequest request = SmsRequest.builder()
                .to("+1234567890")
                .message("Test")
                .build();

        mockMvc.perform(post("/api/sms/send/INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
