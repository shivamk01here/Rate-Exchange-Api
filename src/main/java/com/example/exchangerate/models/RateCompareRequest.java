package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCompareRequest {

    @NotBlank
    private String fromCurrency;

    @NotBlank
    private String toCurrency;

    @Positive
    private BigDecimal amount;
}
