package com.example.exchangerate.favorites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritePair {

    private String id;
    private String fromCurrency;
    private String toCurrency;
    private String label;
    @Builder.Default private Instant createdAt = Instant.now();
}
