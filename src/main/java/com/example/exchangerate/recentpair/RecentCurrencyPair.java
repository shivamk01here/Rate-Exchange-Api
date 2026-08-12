package com.example.exchangerate.recentpair;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentCurrencyPair {

    private String fromCurrency;

    private String toCurrency;

    @Builder.Default private Instant lastUsedAt = Instant.now();

    @Builder.Default private long useCount = 0;
}
