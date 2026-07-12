package com.example.exchangerate.trend;

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
public class RateSnapshot {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private String providerCode;
    @Builder.Default private Instant timestamp = Instant.now();
}
