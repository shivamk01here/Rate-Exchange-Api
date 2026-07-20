package com.example.exchangerate.bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionBookmark {

    private String id;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "fromCurrency is required")
    @Size(min = 3, max = 3, message = "fromCurrency must be a 3-letter currency code")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    @Size(min = 3, max = 3, message = "toCurrency must be a 3-letter currency code")
    private String toCurrency;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Builder.Default private Instant createdAt = Instant.now();
    private Instant lastUsedAt;
    @Builder.Default private long useCount = 0;
}
