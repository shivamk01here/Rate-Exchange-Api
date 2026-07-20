package com.example.exchangerate.apikey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    private String id;

    @NotBlank(message = "API key value is required")
    private String key;

    @NotBlank(message = "Label is required")
    private String label;

    @Positive(message = "Rate limit must be positive")
    @Builder.Default private int requestsPerMinute = 100;

    private boolean enabled;

    @Builder.Default private Instant createdAt = Instant.now();
    private Instant lastUsedAt;
    @Builder.Default private long usageCount = 0;
}
