package com.example.exchangerate.services;

import com.example.exchangerate.models.ConversionRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvExportService {

    private static final String HEADER = "ID,Provider,From,To,Amount,Rate,ConvertedAmount,Status,Timestamp";

    public String exportToCsv(List<ConversionRecord> records) {
        if (records == null || records.isEmpty()) {
            return HEADER;
        }
        String csv = records.stream()
                .map(this::toCsvRow)
                .collect(Collectors.joining("\n", HEADER + "\n", ""));
        log.info("Exported {} conversion records to CSV", records.size());
        return csv;
    }

    private String toCsvRow(ConversionRecord record) {
        return String.join(",",
                escapeCsv(record.getId()),
                escapeCsv(record.getProviderCode() != null ? record.getProviderCode().name() : ""),
                escapeCsv(record.getFromCurrency()),
                escapeCsv(record.getToCurrency()),
                escapeCsv(record.getAmount() != null ? record.getAmount().toPlainString() : ""),
                escapeCsv(record.getRate() != null ? record.getRate().toPlainString() : ""),
                escapeCsv(record.getConvertedAmount() != null ? record.getConvertedAmount().toPlainString() : ""),
                escapeCsv(record.getStatus()),
                escapeCsv(record.getTimestamp() != null ? record.getTimestamp().toString() : ""));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
