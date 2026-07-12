package com.example.exchangerate.trend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendSummary {

    private String fromCurrency;
    private String toCurrency;
    private int totalSnapshots;
    private BigDecimal latestRate;
    private BigDecimal oldestRate;
    private BigDecimal highestRate;
    private BigDecimal lowestRate;
    private BigDecimal averageRate;
    private BigDecimal overallPercentChange;
    private RateTrend.TrendDirection overallDirection;
    private List<RateTrend> recentTrends;
    @Builder.Default private Instant generatedAt = Instant.now();
}
