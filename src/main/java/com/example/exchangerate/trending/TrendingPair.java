package com.example.exchangerate.trending;

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
public class TrendingPair {

    private String fromCurrency;

    private String toCurrency;

    private long conversionCount;

    private BigDecimal totalVolume;

    private Instant latestTimestamp;
}
