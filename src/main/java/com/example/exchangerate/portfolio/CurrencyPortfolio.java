package com.example.exchangerate.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPortfolio {

    private String id;

    private String name;

    private String baseCurrency;

    @Builder.Default
    private Map<String, BigDecimal> holdings = new LinkedHashMap<>();

    @Builder.Default private Instant createdAt = Instant.now();
}
