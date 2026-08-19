package com.example.exchangerate.notes;

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
public class ConversionNote {

    private String id;

    @NotBlank(message = "fromCurrency is required")
    @Size(min = 3, max = 3, message = "fromCurrency must be a 3-letter currency code")
    private String fromCurrency;

    @NotBlank(message = "toCurrency is required")
    @Size(min = 3, max = 3, message = "toCurrency must be a 3-letter currency code")
    private String toCurrency;

    @NotBlank(message = "Note text is required")
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String noteText;

    private Instant createdAt;
    private Instant updatedAt;
}
