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
public class ExchangeRateResponse {

    private ProviderCodes providerCode;
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal convertedAmount;
    private String status;
    @Builder.Default private Instant timestamp = Instant.now();

    public static ExchangeRateResponse fromProviderResponse(ProviderCodes providerCode, ExchangeRateRequest request, ProviderRateResponse providerResponse) {
        return ExchangeRateResponse.builder()
                .providerCode(providerCode)
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .amount(request.getAmount())
                .rate(providerResponse.getRate())
                .convertedAmount(request.getAmount().multiply(providerResponse.getRate()))
                .status("SUCCESS")
                .timestamp(Instant.now())
                .build();
    }

    public static ExchangeRateResponse failed(ProviderCodes providerCode, ExchangeRateRequest request, String reason) {
        return ExchangeRateResponse.builder()
                .providerCode(providerCode)
                .fromCurrency(request != null ? request.getFromCurrency() : null)
                .toCurrency(request != null ? request.getToCurrency() : null)
                .amount(request != null ? request.getAmount() : null)
                .rate(BigDecimal.ZERO)
                .convertedAmount(BigDecimal.ZERO)
                .status("FAILED_" + reason)
                .timestamp(Instant.now())
                .build();
    }

    public String toPipeFormat() {
        return String.join("|",
                providerCode != null ? providerCode.name() : "",
                fromCurrency != null ? fromCurrency : "",
                toCurrency != null ? toCurrency : "",
                amount != null ? amount.toPlainString() : "",
                rate != null ? rate.toPlainString() : "",
                convertedAmount != null ? convertedAmount.toPlainString() : "",
                status != null ? status : "");
    }

    public static ExchangeRateResponse fromPipeFormat(String pipe) {
        String[] parts = pipe.split("\\|", -1);
        return ExchangeRateResponse.builder()
                .providerCode(parts[0].isEmpty() ? null : ProviderCodes.valueOf(parts[0]))
                .fromCurrency(parts[1])
                .toCurrency(parts[2])
                .amount(parts[3].isEmpty() ? null : new BigDecimal(parts[3]))
                .rate(parts[4].isEmpty() ? null : new BigDecimal(parts[4]))
                .convertedAmount(parts[5].isEmpty() ? null : new BigDecimal(parts[5]))
                .status(parts.length > 6 ? parts[6] : "")
                .build();
    }
}
