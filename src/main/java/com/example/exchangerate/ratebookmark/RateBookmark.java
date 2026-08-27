package com.example.exchangerate.ratebookmark;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateBookmark {

    private String id;

    @NotBlank(message = "fromCurrency is required")
    @Size(min = 3, max = 3, message = "fromCurrency must be a 3-letter currency code")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    @Size(min = 3, max = 3, message = "toCurrency must be a 3-letter currency code")
    private String toCurrency;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be a positive number")
    private BigDecimal rate;

    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    private String providerCode;

    private Instant bookmarkedAt;
}
