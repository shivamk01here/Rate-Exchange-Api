package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryPageRequest {

    private int page;
    private int size;
    private String fromCurrency;
    private String toCurrency;
    private Long fromEpochMillis;
    private Long toEpochMillis;
}
