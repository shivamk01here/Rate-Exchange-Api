package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionQueryParams {

    private String from;
    private String to;
    private BigDecimal amount;

    public ExchangeRateRequest toExchangeRateRequest() {
        return ExchangeRateRequest.builder()
                .fromCurrency(from != null ? from.toUpperCase() : null)
                .toCurrency(to != null ? to.toUpperCase() : null)
                .amount(amount)
                .build();
    }
}
