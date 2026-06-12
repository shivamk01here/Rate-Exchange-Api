package com.example.exchangerate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRecord {

    private String id;
    private ProviderCodes providerCode;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal convertedAmount;
    private String status;
    private Instant timestamp;

    public String toPipeFormat() {
        return String.join("|",
                id,
                providerCode != null ? providerCode.name() : "",
                fromCurrency != null ? fromCurrency : "",
                toCurrency != null ? toCurrency : "",
                amount != null ? amount.toPlainString() : "",
                rate != null ? rate.toPlainString() : "",
                convertedAmount != null ? convertedAmount.toPlainString() : "",
                status != null ? status : "",
                timestamp != null ? String.valueOf(timestamp.toEpochMilli()) : "");
    }

    public static ConversionRecord fromPipeFormat(String pipe) {
        String[] parts = pipe.split("\\|", -1);
        return ConversionRecord.builder()
                .id(parts[0])
                .providerCode(parts[1].isEmpty() ? null : ProviderCodes.valueOf(parts[1]))
                .fromCurrency(parts[2])
                .toCurrency(parts[3])
                .amount(parts[4].isEmpty() ? null : new BigDecimal(parts[4]))
                .rate(parts[5].isEmpty() ? null : new BigDecimal(parts[5]))
                .convertedAmount(parts[6].isEmpty() ? null : new BigDecimal(parts[6]))
                .status(parts.length > 7 ? parts[7] : "")
                .timestamp(parts.length > 8 && !parts[8].isEmpty()
                        ? Instant.ofEpochMilli(Long.parseLong(parts[8]))
                        : null)
                .build();
    }
}
