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
public class ExchangeRateRequest {

    @NotBlank
    private String fromCurrency;

    @NotBlank
    private String toCurrency;

    @NotNull
    @Positive
    private BigDecimal amount;

    public String toPipeFormat() {
        return String.join("|",
                fromCurrency.toUpperCase(),
                toCurrency.toUpperCase(),
                amount.toPlainString());
    }

    public static ExchangeRateRequest fromPipeFormat(String pipeString) {
        if (pipeString == null || pipeString.isBlank()) {
            throw new IllegalArgumentException("Pipe string must not be blank");
        }
        String[] parts = pipeString.split("\\|");
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid pipe format: expected 3 fields (fromCurrency|toCurrency|amount), got " + parts.length);
        }
        return ExchangeRateRequest.builder()
                .fromCurrency(parts[0])
                .toCurrency(parts[1])
                .amount(new BigDecimal(parts[2]))
                .build();
    }
}
