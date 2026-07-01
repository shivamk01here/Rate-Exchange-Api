package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCompareRequest {

    @NotBlank
    private String fromCurrency;

    @NotBlank
    private String toCurrency;
}
