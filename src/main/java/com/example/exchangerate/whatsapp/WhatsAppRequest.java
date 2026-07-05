package com.example.exchangerate.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppRequest {

    @NotBlank
    private String to;

    @NotBlank
    private String message;

    private String from;
}
