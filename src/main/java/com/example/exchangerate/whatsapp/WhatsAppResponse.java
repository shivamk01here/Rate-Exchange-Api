package com.example.exchangerate.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppResponse {

    private boolean success;
    private String messageId;
    private WhatsAppProviderType provider;
    private String errorMessage;

    public static WhatsAppResponse success(WhatsAppRequest request, WhatsAppProviderType provider, String messageId) {
        return WhatsAppResponse.builder()
                .success(true)
                .messageId(messageId)
                .provider(provider)
                .build();
    }

    public static WhatsAppResponse failed(WhatsAppRequest request, WhatsAppProviderType provider, String errorMessage) {
        return WhatsAppResponse.builder()
                .success(false)
                .provider(provider)
                .errorMessage(errorMessage)
                .build();
    }
}
