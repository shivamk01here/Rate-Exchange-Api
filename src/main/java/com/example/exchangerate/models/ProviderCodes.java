package com.example.exchangerate.models;

public enum ProviderCodes {
    EXCHANGE_RATE_API("ExchangeRateApi"),
    OPEN_EXCHANGE_RATES("OpenExchangeRates");

    private final String title;

    ProviderCodes(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}