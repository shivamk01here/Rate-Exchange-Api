package com.example.exchangerate.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioValuation {

    private String portfolioId;
    private String portfolioName;
    private String baseCurrency;
    private Map<String, BigDecimal> holdings;
    private Map<String, HoldingValue> holdingValues;
    private BigDecimal totalValue;
    private String status;
    private Instant valuedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldingValue {
        private String currency;
        private BigDecimal amount;
        private BigDecimal rate;
        private BigDecimal convertedValue;
        private String status;
    }
}
