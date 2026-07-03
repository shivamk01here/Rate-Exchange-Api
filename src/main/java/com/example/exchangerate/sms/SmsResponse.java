package com.example.exchangerate.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {

    private boolean success;
    private String messageId;
    private SmsProviderType provider;
    private String errorMessage;

    public static SmsResponse success(SmsRequest request, SmsProviderType provider, String messageId) {
        return SmsResponse.builder()
                .success(true)
                .messageId(messageId)
                .provider(provider)
                .build();
    }

    public static SmsResponse failed(SmsRequest request, SmsProviderType provider, String errorMessage) {
        return SmsResponse.builder()
                .success(false)
                .provider(provider)
                .errorMessage(errorMessage)
                .build();
    }
}
