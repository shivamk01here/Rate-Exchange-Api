package com.example.exchangerate.aliases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateAlias {

    private String id;
    private String alias;
    private String fromCurrency;
    private String toCurrency;
    private Instant createdAt;
}
