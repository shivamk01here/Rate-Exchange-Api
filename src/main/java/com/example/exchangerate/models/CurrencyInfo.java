package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyInfo {
    private String code;
    private String name;
    private String symbol;
    private int decimalPlaces;
    private int numericCode;
}
