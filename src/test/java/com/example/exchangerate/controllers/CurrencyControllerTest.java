package com.example.exchangerate.controllers;

import com.example.exchangerate.models.CurrencyInfo;
import com.example.exchangerate.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyControllerTest {

    private CurrencyController currencyController;

    @BeforeEach
    void setUp() {
        CurrencyService currencyService = new CurrencyService();
        currencyController = new CurrencyController(currencyService);
    }

    @Test
    void getSupportedCurrencies_returnsAllCurrencies() {
        Collection<CurrencyInfo> currencies = currencyController.getSupportedCurrencies();
        assertNotNull(currencies);
        assertFalse(currencies.isEmpty());
    }

    @Test
    void getSupportedCurrencies_containsExpectedCurrencies() {
        Collection<CurrencyInfo> currencies = currencyController.getSupportedCurrencies();
        assertTrue(currencies.stream().anyMatch(c -> "USD".equals(c.getCode())));
        assertTrue(currencies.stream().anyMatch(c -> "EUR".equals(c.getCode())));
        assertTrue(currencies.stream().anyMatch(c -> "GBP".equals(c.getCode())));
    }

    @Test
    void getCurrency_returnsCurrencyForValidCode() {
        CurrencyInfo usd = currencyController.getCurrency("USD");
        assertNotNull(usd);
        assertEquals("USD", usd.getCode());
        assertEquals("$", usd.getSymbol());
    }

    @Test
    void getCurrency_returnsCurrencyCaseInsensitive() {
        CurrencyInfo currency = currencyController.getCurrency("eur");
        assertNotNull(currency);
        assertEquals("EUR", currency.getCode());
    }

    @Test
    void getCurrency_throwsExceptionForInvalidCode() {
        assertThrows(ResponseStatusException.class, () -> currencyController.getCurrency("XYZ"));
    }

    @Test
    void getCurrency_throwsExceptionForEmptyCode() {
        assertThrows(ResponseStatusException.class, () -> currencyController.getCurrency(""));
    }
}
