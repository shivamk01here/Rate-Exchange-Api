package com.example.exchangerate.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledReport {

    private String id;

    @NotBlank(message = "Report name is required")
    private String name;

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotEmpty(message = "At least one currency pair is required")
    private List<CurrencyPair> currencyPairs;

    @NotBlank(message = "Email recipient is required")
    private String email;

    @Builder.Default private boolean enabled = true;

    @Builder.Default private Instant createdAt = Instant.now();
    private Instant lastGeneratedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyPair {
        @NotBlank private String from;
        @NotBlank private String to;
    }
}
