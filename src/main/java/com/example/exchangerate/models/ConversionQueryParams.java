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
}
