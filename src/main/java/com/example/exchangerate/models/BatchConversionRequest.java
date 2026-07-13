package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchConversionRequest {

    @NotBlank
    private String fromCurrency;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotEmpty
    private List<@NotBlank String> toCurrencies;
}
