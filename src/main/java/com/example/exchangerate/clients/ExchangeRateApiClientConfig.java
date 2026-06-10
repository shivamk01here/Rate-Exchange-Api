package com.example.exchangerate.clients;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExchangeRateApiClientConfig extends ProviderClientConfig {

    @NotBlank
    String apiKey;

    @NotBlank
    String merchantId;
}
