package com.example.exchangerate.trend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateTrend {

    private String id;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private String providerCode;
    private TrendDirection direction;
    private BigDecimal percentChange;
    private Instant recordedAt;

    public enum TrendDirection {
        RISING, FALLING, STABLE
    }
}
