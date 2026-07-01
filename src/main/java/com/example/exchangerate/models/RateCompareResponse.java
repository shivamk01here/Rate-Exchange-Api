package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCompareResponse {

    private String fromCurrency;
    private String toCurrency;
    private List<ProviderRateDetail> providerRates;
    @Builder.Default private Instant timestamp = Instant.now();
}
