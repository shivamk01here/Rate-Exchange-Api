package com.example.exchangerate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("conversion-notes")
public class ConversionNotesConfig {

    private boolean enabled = true;
    private int maxNotes = 200;
    private int maxNoteLength = 500;
}
