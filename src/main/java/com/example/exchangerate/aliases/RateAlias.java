package com.example.exchangerate.aliases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateAlias {

    private String id;

    @NotBlank(message = "Alias is required")
    @Size(min = 2, max = 30, message = "Alias must be between 2 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Alias can only contain letters, numbers, hyphens and underscores")
    private String alias;

    @NotBlank(message = "fromCurrency is required")
    @Size(min = 3, max = 3, message = "fromCurrency must be a 3-letter currency code")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    @Size(min = 3, max = 3, message = "toCurrency must be a 3-letter currency code")
    private String toCurrency;

    private Instant createdAt;
}
