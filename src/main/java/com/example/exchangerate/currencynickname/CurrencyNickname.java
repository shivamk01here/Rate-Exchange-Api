package com.example.exchangerate.currencynickname;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyNickname {

    private String id;

    @NotBlank(message = "currencyCode is required")
    @Size(min = 3, max = 3, message = "currencyCode must be a 3-letter currency code")
    private String currencyCode;

    @NotBlank(message = "nickname is required")
    @Size(max = 100, message = "nickname must not exceed 100 characters")
    private String nickname;

    @Builder.Default private Instant createdAt = Instant.now();
}
