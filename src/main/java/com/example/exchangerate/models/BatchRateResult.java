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
public class BatchRateResult {

    private String toCurrency;
    private BigDecimal rate;
    private BigDecimal convertedAmount;
    private String status;
}
