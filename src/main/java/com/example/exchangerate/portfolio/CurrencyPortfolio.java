package com.example.exchangerate.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
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

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "baseCurrency is required")
    @Size(min = 3, max = 3, message = "baseCurrency must be a 3-letter currency code")
    private String baseCurrency;

    @Builder.Default
    private Map<String, BigDecimal> holdings = new LinkedHashMap<>();

    @Builder.Default private Instant createdAt = Instant.now();
}
