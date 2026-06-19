package com.example.exchangerate.services;

import com.example.exchangerate.config.ExportConfig;
import com.example.exchangerate.models.ConversionRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JsonExportService {

    private final ObjectMapper objectMapper;
    private final ExportConfig exportConfig;

    public JsonExportService(ExportConfig exportConfig) {
        this.exportConfig = exportConfig;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        if (exportConfig.getJson().isIncludeNullFields()) {
            objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS);
        }
    }

    private static final Set<String> ALL_FIELDS = Set.of(
            "id", "providerCode", "fromCurrency", "toCurrency",
            "amount", "rate", "convertedAmount", "status", "timestamp");

    public String exportToJson(List<ConversionRecord> records) {
        return exportToJson(records, false, ALL_FIELDS);
    }

    public String exportToJson(List<ConversionRecord> records, boolean prettyPrint) {
        return exportToJson(records, prettyPrint, ALL_FIELDS);
    }

    public String exportToJson(List<ConversionRecord> records, boolean prettyPrint, Set<String> fields) {
        try {
            List<ConversionRecord> safeRecords = records != null ? records : List.of();
            Set<String> resolvedFields = (fields == null || fields.isEmpty()) ? ALL_FIELDS : fields;
            List<Map<String, Object>> filtered = resolvedFields.equals(ALL_FIELDS)
                    ? null
                    : safeRecords.stream()
                        .map(r -> toFilteredMap(r, resolvedFields))
                        .collect(Collectors.toList());
            ObjectMapper mapper = prettyPrint
                    ? objectMapper.writerWithDefaultPrettyPrinter()
                    : objectMapper;
            String json = filtered != null
                    ? mapper.writeValueAsString(filtered)
                    : mapper.writeValueAsString(safeRecords);
            log.info("Exported {} conversion records to JSON", safeRecords.size());
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize conversion records to JSON", e);
            return "[]";
        }
    }

    private Map<String, Object> toFilteredMap(ConversionRecord record, Set<String> fields) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fields.contains("id")) map.put("id", record.getId());
        if (fields.contains("providerCode")) map.put("providerCode",
                record.getProviderCode() != null ? record.getProviderCode().name() : null);
        if (fields.contains("fromCurrency")) map.put("fromCurrency", record.getFromCurrency());
        if (fields.contains("toCurrency")) map.put("toCurrency", record.getToCurrency());
        if (fields.contains("amount")) map.put("amount", record.getAmount());
        if (fields.contains("rate")) map.put("rate", record.getRate());
        if (fields.contains("convertedAmount")) map.put("convertedAmount", record.getConvertedAmount());
        if (fields.contains("status")) map.put("status", record.getStatus());
        if (fields.contains("timestamp")) map.put("timestamp", record.getTimestamp());
        return map;
    }
}
