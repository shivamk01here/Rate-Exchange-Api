package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionQueryParams {

    @NotBlank(message = "from currency is required")
    private String from;

    @NotBlank(message = "to currency is required")
    private String to;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    public ExchangeRateRequest toExchangeRateRequest() {
        return ExchangeRateRequest.builder()
                .fromCurrency(from != null ? from.toUpperCase() : null)
                .toCurrency(to != null ? to.toUpperCase() : null)
                .amount(amount)
                .build();
    }
}
