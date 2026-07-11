package com.example.exchangerate.favorites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRateResult {

    private String favoriteId;
    private String fromCurrency;
    private String toCurrency;
    private String label;
    private BigDecimal rate;
    private String status;
    private String provider;
}
