package com.example.exchangerate.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorHistory {

    private String id;

    @NotBlank(message = "fromCurrency is required")
    @Size(min = 3, max = 3, message = "fromCurrency must be a 3-letter currency code")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    @Size(min = 3, max = 3, message = "toCurrency must be a 3-letter currency code")
    private String toCurrency;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    private BigDecimal convertedAmount;

    private String provider;

    private boolean favorite;

    @Builder.Default private Instant calculatedAt = Instant.now();
}
