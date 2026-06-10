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
public class ProviderRateResponse {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private String providerReference;
    private long timestamp;

    public static ProviderRateResponse fromPipeFormat(String pipeString) {
        String[] parts = pipeString.split("\\|");
        return ProviderRateResponse.builder()
                .fromCurrency(parts[0])
                .toCurrency(parts[1])
                .rate(new BigDecimal(parts[2]))
                .providerReference(parts[3])
                .timestamp(Long.parseLong(parts[4]))
                .build();
    }

    public String toPipeFormat() {
        return String.join("|",
                fromCurrency,
                toCurrency,
                rate.toPlainString(),
                providerReference,
                String.valueOf(timestamp));
    }
}
