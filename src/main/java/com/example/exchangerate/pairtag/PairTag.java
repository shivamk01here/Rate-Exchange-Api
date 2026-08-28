package com.example.exchangerate.pairtag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PairTag {

    private String id;

    @NotBlank(message = "fromCurrency is required")
    @Size(min = 3, max = 3, message = "fromCurrency must be a 3-letter currency code")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    @Size(min = 3, max = 3, message = "toCurrency must be a 3-letter currency code")
    private String toCurrency;

    @NotBlank(message = "Tag is required")
    @Size(max = 50, message = "Tag must not exceed 50 characters")
    private String tag;

    private Instant createdAt;
}
