package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRateDetail {

    private ProviderCodes providerCode;
    private BigDecimal rate;
    private String status;
    @Builder.Default private Instant timestamp = Instant.now();
}
