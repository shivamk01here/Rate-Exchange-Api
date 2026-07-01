package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateCompareResponse {

    private String fromCurrency;
    private String toCurrency;
    private List<ProviderRateDetail> providerRates;
    private BigDecimal bestRate;
    private ProviderCodes bestProvider;
    @Builder.Default private Instant timestamp = Instant.now();

    public void computeBest() {
        this.providerRates.stream()
                .filter(d -> "SUCCESS".equals(d.getStatus()) && d.getRate() != null)
                .max(Comparator.comparing(ProviderRateDetail::getRate))
                .ifPresent(best -> {
                    this.bestRate = best.getRate();
                    this.bestProvider = best.getProviderCode();
                });
    }
}
