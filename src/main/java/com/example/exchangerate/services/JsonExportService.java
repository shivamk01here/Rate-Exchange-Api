package com.example.exchangerate.services;

import com.example.exchangerate.models.ConversionRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JsonExportService {

    private final ObjectMapper objectMapper;

    public JsonExportService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String exportToJson(List<ConversionRecord> records) {
        try {
            String json = objectMapper.writeValueAsString(
                    records != null ? records : List.of());
            log.info("Exported {} conversion records to JSON", records != null ? records.size() : 0);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize conversion records to JSON", e);
            return "[]";
        }
    }
}
