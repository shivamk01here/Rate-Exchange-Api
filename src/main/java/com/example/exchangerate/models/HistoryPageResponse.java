package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryPageResponse {

    private List<ConversionRecord> records;
    private int page;
    private int size;
    private long totalRecords;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
