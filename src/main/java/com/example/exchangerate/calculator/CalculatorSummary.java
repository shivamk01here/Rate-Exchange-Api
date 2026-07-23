package com.example.exchangerate.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculatorSummary {

    private long totalConversions;
    private long favoriteCount;
    private BigDecimal totalAmountConverted;
    private BigDecimal averageRate;
    private String mostUsedPair;
    private String mostUsedProvider;
    private Map<String, Long> pairFrequency;
    private Map<String, Long> providerFrequency;
    private List<String> uniqueCurrencies;
    private Instant generatedAt;
}
