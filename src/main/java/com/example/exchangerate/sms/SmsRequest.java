package com.example.exchangerate.sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    @NotBlank
    private String to;

    @NotBlank
    private String message;

    private String from;
}
