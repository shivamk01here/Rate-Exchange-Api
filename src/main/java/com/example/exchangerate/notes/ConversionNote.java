package com.example.exchangerate.notes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionNote {

    private String id;
    private String fromCurrency;
    private String toCurrency;
    private String noteText;
    private Instant createdAt;
    private Instant updatedAt;
}
