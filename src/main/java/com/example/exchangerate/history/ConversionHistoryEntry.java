package com.example.exchangerate.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionHistoryEntry {

    private String id;

    @NotBlank(message = "fromCurrency is required")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    private String toCurrency;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Rate is required")
    private BigDecimal rate;

    @NotNull(message = "Converted amount is required")
    private BigDecimal convertedAmount;

    private String provider;

    private String status;

    @Builder.Default private Instant timestamp = Instant.now();

    private String clientIp;

    private String userAgent;
}
